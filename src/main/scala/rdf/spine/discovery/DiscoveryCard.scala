package rdf.spine.discovery

import java.util.concurrent.TimeUnit

import org.apache.spark.{SparkConf, SparkContext}
import org.openrdf.rio.RDFFormat
import org.slf4j.LoggerFactory
import rdf.spine.model.CardCandidate
import rdf.spine.parser.CardinalityConstraint
import rdf.spine.util.{MemoryUtils, TimeWatch}

/**
  * Discovery of Cardinality Constraints.
  *
  * @author Emir Munoz
  * @since 05/10/15.
  * @version 0.0.5
  */
object DiscoveryCard {

  val _log = LoggerFactory.getLogger(DiscoveryCard.getClass.getSimpleName)

  val usage =
    """
      Arguments: [numPartitions] [RDFFile]
    """.stripMargin

  /**
    * Main method.
    *
    * @param args Params
    */
  def main(args: Array[String]): Unit = {

    if (args.length != 3) println(usage)
    val argList = args.toList

    // Params
    val numParts = argList(0).toInt
    val filename = argList(1).toString
    val constContext = argList(2).toString

    val conf = new SparkConf()
      .setAppName(s"DiscoveryCard")
      .set("spark.executor.memory", "512m")
    val sc = new SparkContext(conf)

    _log.info("Starting discovery of cardinality constraints from RDF data")
    if (!constContext.isEmpty) {
      _log.info("Context is limited to class '{}'", constContext)
    }
    else {
      _log.info("Context is not specified")
    }

    MemoryUtils.printMemoryInfo()
    val initialProjections = ProjectionOp.getProjections(filename, RDFFormat.NTRIPLES, new CardinalityConstraint)
    MemoryUtils.printMemoryInfo()

    // start counting execution time
    val time: TimeWatch = TimeWatch.start

    // rdd has a shape Seq[proj: Projection]
    val rdd = sc.parallelize(initialProjections, numParts).cache()
    val rddSize = ProjectionOp.getNumSubjects
    _log.info(s"Number of different subjects: $rddSize")

    // print RDD to stdout
    //rdd.collect().foreach(proj => println(proj))

    // rdd2 has a shape Seq[(property: String, proj: Projection)], where the proj object contains a single property
    val rdd2 = rdd.flatMap(x => ProjectionOp.breakProjection(x))
    //rdd2.foreach(println)

    // rdd3 has a shape Seq[(property: String, proj: Projection)]
    val rdd3 = rdd2.reduceByKey(JoinOp.joinSingle)
    rdd3.foreach(candidate => _log.info(new CardCandidate(candidate._2, constContext, rddSize.toInt).toString))

    MemoryUtils.printMemoryInfo()
    _log.info("Elapsed time={}ms and {}s", time.time, time.time(TimeUnit.SECONDS))

    val candidatesShEx: StringBuilder = new StringBuilder()

    // here we need to collect in order to create the string builder
    rdd3.collect.foreach(candidate => candidatesShEx.append("\n").append(new CardCandidate(candidate._2, constContext, rddSize.toInt).toShEx))

    val shEx =
      s"""
      PREFIX ex: <http://example.com>
      PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
      start=<AShape>

      <AShape> {
        $candidatesShEx
      }
      """

    _log.info(s"ShEx shape: $shEx")

    sc.stop()
  }

}
