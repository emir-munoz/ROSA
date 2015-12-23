package spine.discovery

import org.apache.logging.log4j.LogManager
import org.apache.spark.mllib.fpm.FPGrowth
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.openrdf.rio.RDFFormat
import spine.model.{CardCandidate, Projection}
import spine.parser.CardinalityConstraint

/**
  * Discovery of Cardinality Constraints.
  *
  * @author Emir Munoz
  * @since 05/10/15.
  * @version 0.0.5
  */
object DiscoveryCard {

  val _log = LogManager.getLogger(DiscoveryCard.getClass.getSimpleName)

  val usage =
    """
      Arguments: [numPartitions] [RDFFile]
    """.stripMargin

  /**
    * Main method.
    * @param args Params
    */
  def main(args: Array[String]): Unit = {

    if (args.length != 3) println(usage)
    val argList = args.toList

    // Params
    val numParts = argList(0).toInt
    val filename = argList(1).toString
    val rdfClass = argList(2).toString

    val conf = new SparkConf()
      .setAppName(s"DiscoveryCard")
      .set("spark.executor.memory", "512m")
    val sc = new SparkContext(conf)

    //val filename = "/media/sf_projects/spine-ldd/rdf-data/bands-dbpedia-small-corpus.nt"
    val initialProjections = ProjectionOp.getProjections(filename, RDFFormat.NTRIPLES, new CardinalityConstraint)

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

    rdd3.foreach(candidate => _log.info(new CardCandidate(candidate._2, rdfClass, rddSize.toInt).toString))

    val candidatesShEx: StringBuilder = new StringBuilder()
    // here we need to collect in order to create the string builder
    rdd3.collect.foreach(candidate => candidatesShEx.append("\n").append(new CardCandidate(candidate._2, rdfClass, rddSize.toInt).toShEx))

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
