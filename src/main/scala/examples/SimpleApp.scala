package examples

import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

/**
  * @author Emir Munoz
  * @since 27/01/15
  */
object SimpleApp {

  private val _log = LoggerFactory.getLogger(SimpleApp.getClass.getSimpleName)

  def main(args: Array[String]) {
    val logFile = "/home/emir/work/spark-1.4.0/README.md"
    val conf = new SparkConf().setAppName("Word Count Application")
      //.setMaster("spark://10.0.2.15:39448")
      .setMaster("local")
      .set("spark.executor.memory", "100m")
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    _log.info("Lines with a: %s, Lines with b: %s".format(numAs, numBs))

    sc.stop()
  }

}
