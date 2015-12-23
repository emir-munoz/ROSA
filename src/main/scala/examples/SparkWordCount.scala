package examples

import org.apache.logging.log4j.LogManager
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Scala word count using Spark.
 *
 * Created by Emir Munoz on 29/01/15.
 */
object SparkWordCount {

  val _log = LogManager.getLogger(SparkWordCount.getClass.getSimpleName);

  def sanitizeString(s: String): String = {
    val sTmp: String = s.replaceAll("[^a-zA-Z ]", "").toLowerCase.trim
    if (sTmp.length() > 0)
      sTmp
    else
      None.toString
  }

  def main(args: Array[String]) {

    val threshold_tmp: String = "2"
    val logFile: String = "/home/emir/work/spark-1.4.0/README.md"

    val args = Array(logFile, threshold_tmp)

    val sc = new SparkContext(new SparkConf().setAppName("Spark Count").setMaster("local"))
    val threshold = args(1).toInt

    // split each occurrence into words
    val tokenized = sc.textFile(args(0)).flatMap(_.split(" "))

    // filter by cleansing punctuations
    val tokenizedFiltered = tokenized collect { case s: String if (sanitizeString(s) != None.toString) => sanitizeString(s)}
    //val tokenizedFiltered = tokenized.map(term => sanitizeString(term))

    // count the occurrence of each word
    val wordCounts = tokenizedFiltered.map(s => (s, 1)).reduceByKey((a, b) => a + b)

    // filter out words with less than threshold occurrences
    val filtered = wordCounts.filter(_._2 >= threshold)

    // count characters
    val charCounts = filtered.flatMap(_._1.toCharArray).map((_, 1)).reduceByKey(_ + _)

    _log.info(filtered.collect().mkString(", "))
    //println(charCounts.collect().mkString(", "))

    sc.stop()
  }

}
