//package mllib
//
//import org.apache.logging.log4j.LogManager
//import org.apache.spark.mllib.fpm.FPGrowth
//import org.apache.spark.rdd.RDD
//import org.apache.spark.{SparkConf, SparkContext}
//import org.openrdf.rio.RDFFormat
//import spine.parser.CardinalityConstraint
//
///**
// * Parse a RDF file in N-Triples format, and generate transactions to apply the association rules mining.
// *
// * @author Emir Munoz
// * @since 05/10/15.
// * @version 0.0.5
// */
//object RDFFreqItemsets {
//
//  val _log = LogManager.getLogger("RDFFreqItemsets")
//
//  val usage =
//    """
//      Arguments: [minsupport] [numpart] [confidence]
//    """.stripMargin
//
//  /**
//   * Main method.
//   * @param args Params
//   */
//  def main(args: Array[String]): Unit = {
//    if (args.length == 0) println(usage)
//    val argList = args.toList
//
//    val conf = new SparkConf()
//      .setAppName(s"FPGrowthExample")
//      .set("spark.executor.memory", "512m")
//    val sc = new SparkContext(conf)
//
//    //val filename = "/media/sf_projects/spine-ldd/countries-dbpedia-clean.nt"
//    //val filename = "/media/sf_projects/spine-ldd/rdf-data/countries-dbpedia-full-first25k.nt.gz"
//    val filename = "/media/sf_projects/spine-ldd/rdf-data/bands-dbpedia-small-corpus.nt"
//    val transactions = Transactions.getSetOfTransactions(filename, RDFFormat.NTRIPLES, new CardinalityConstraint)
//    //val transactions = Transactions.getTransactions(filename)
//    val rdd = sc.parallelize(transactions, 2).cache()
//    // print RDD to stdout
//    //rdd.collect().foreach(item => println(item.mkString(", ")))
//
//    // minSup = 0.5
//    // numParts = 2
//    // confidence = 0.8
//    runItemsetsMining(argList(0).toDouble, argList(1).toInt, argList(2).toDouble, rdd)
//
//    sc.stop()
//  }
//
//  /**
//   * Call to FP-Growth parallel implementation
//   *
//   * @param minSup Minimum support
//   * @param numParts Number of partitions
//   * @param confidence Confidence score
//   * @param rdd Transactions database
//   */
//  def runItemsetsMining(minSup: Double, numParts: Int, confidence: Double, rdd: RDD[Array[String]]) = {
//    val fpg = new FPGrowth()
//
//    val model = fpg
//      .setMinSupport(minSup)
//      .setNumPartitions(numParts)
//      .run(rdd)
//
//    _log.info(s"Number of frequent itemsets ${model.freqItemsets.count()}")
////    //println(s"Number of frequent itemsets: ${model.freqItemsets.count()}")
////    model.freqItemsets.collect().foreach { itemset =>
////      // println(itemset.items.mkString("{", ",", "}") + ", " + itemset.freq)
////      _log.info("[ITEMSET] " + itemset.items.mkString("{", ",", "}") + ", " + itemset.freq)
////    }
//
//    // Sort the itemsets across the machines.
//    // By calling collect we get a merged sorted list instead of the local sorted list computed by each worker.
//    val sortedItemsets = model.freqItemsets.sortBy(_.items.length, ascending = true).collect()
//    for (itemset <- sortedItemsets) {
//      _log.info(s"[SORTED_ITEMSETS] " + itemset.items.mkString("[", ",", "]") + ", size=" + itemset.items.length + ", frequency=" + itemset.freq)
//    }
//
////    println(s"Association rules:")
////    val minConfidence = confidence
////    model.generateAssociationRules(minConfidence)
////      .sortBy(_.confidence, ascending = true)
////      .collect()
////      .foreach { rule =>
////      //      println(
////      //        rule.antecedent.mkString("[", ",", "]")
////      //          + " => " + rule.consequent.mkString("[", ",", "]")
////      //          + ", " + rule.confidence)
////
////      //if (rule.consequent.contains("<http://dbpedia.org/ontology/Country>")) {
////      _log.info("[RULE] " + rule.antecedent.mkString("[", ", ", "]")
////        + " => " + rule.consequent.mkString("[", ", ", "]")
////        + ", " + rule.confidence)
////      //}
////    }
//  }
//
//}
