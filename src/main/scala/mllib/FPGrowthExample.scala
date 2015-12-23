package mllib

import org.apache.spark.mllib.fpm.FPGrowth
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Test of FP-Growth.
 *
 * https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/mllib/FPGrowthExample.scala
 *
 * @author Emir Munoz
 * @since 27/08/15.
 */
object FPGrowthExample {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(s"FPGrowthExample")
    val sc = new SparkContext(conf)

    val transactions = Seq(
      //      "r z h k p",
      //      "z y x w v u t s",
      //      "s x o n r",
      //      "x z y m t s q e",
      //      "z",
      //      "x z y r q t p")

      "dbp:name#count:3 dbp:address#count:5",
      "dbp:name#count:3 dbp:address#count:5",
      "dbp:name#count:3 dbp:address#count:4",
      "dbp:name#count:2 dbp:address#count:4")

      .map(_.split(" "))
    val rdd = sc.parallelize(transactions, 2).cache()

    val fpg = new FPGrowth()

    val model = fpg
      .setMinSupport(0.3)
      .setNumPartitions(2)
      .run(rdd)

    println(s"Number of frequent itemsets: ${model.freqItemsets.count()}")

    model.freqItemsets.collect().foreach { itemset =>
      println(itemset.items.mkString("{", ",", "}") + ", " + itemset.freq)
    }

    val minConfidence = 0.6
    model.generateAssociationRules(minConfidence).collect().foreach { rule =>
      println(
        rule.antecedent.mkString("[", ",", "]")
          + " => " + rule.consequent.mkString("[", ",", "]")
          + ", " + rule.confidence)
    }

    sc.stop()
  }

}
