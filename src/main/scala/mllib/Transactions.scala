//package mllib
//
//import java.io._
//import java.util.zip.GZIPInputStream
//
//import org.apache.logging.log4j.LogManager
//import org.openrdf.rio.helpers.{BasicParserSettings, NTriplesParserSettings}
//import org.openrdf.rio.nquads.NQuadsParser
//import org.openrdf.rio.ntriples.NTriplesParser
//import org.openrdf.rio.turtle.TurtleParser
//import org.openrdf.rio.{RDFFormat, RDFParser}
//import org.semanticweb.yars.nx.parser.NxParser
//import spine.parser.Constraint
//
//import scala.collection.mutable
//import scala.collection.mutable.ArrayBuffer
//
///**
// * @author Emir Munoz
// * @since 08/10/15.
// */
//object Transactions {
//
//  val _log = LogManager.getLogger("Transactions")
//
//  /**
//   * Read a RDF file line by line and extract type transactions.
//   * @param filename RDF filename.
//   */
//  def getTransactions(filename: String): Seq[Array[String]] = {
//    val is = new FileInputStream(filename)
//    val nxp = new NxParser()
//    nxp.parse(is)
//
//    val trans = mutable.Map[String, Array[String]]()
//    while (nxp.hasNext) {
//      val nxx = nxp.next()
//      //println(nxx(0) + " " + nxx(1) + " " + nxx(2))
//
//      // Transactions made by rdf:type
//      //      if (nxx(1).toString == "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") {
//      //        if (trans.contains(nxx(0).toString)) {
//      //          val types: ArrayBuffer[String] = trans.get(nxx(0).toString).get.to[mutable.ArrayBuffer]
//      //          types.append(nxx(2).toString)
//      //          trans.put(nxx(0).toString, types.toArray)
//      //        } else {
//      //          trans.put(nxx(0).toString, Array(nxx(2).toString))
//      //        }
//      //      }
//
//      // Transactions made by properties
//      if (trans.contains(nxx(0).toString)) {
//        val types: ArrayBuffer[String] = trans.get(nxx(0).toString).get.to[mutable.ArrayBuffer]
//        if (!types.contains(nxx(1).toString)) {
//          types.append(nxx(1).toString)
//          trans.put(nxx(0).toString, types.toArray)
//        }
//      } else {
//        trans.put(nxx(0).toString, Array(nxx(1).toString))
//      }
//    }
//
//    // print transactions to stdout
//    // trans.foreach { case (key, value) => println(key + "-->" + value.mkString(", ")) }
//
//    trans.values.toSeq
//  }
//
//  /**
//   *
//   * @param filename
//   * @param format
//   * @param handler
//   * @return
//   */
//  // Seq[Array[String]]
//  def getSetOfTransactions(filename: String, format: RDFFormat, handler: Constraint): mutable.Map[String, mutable.Map[String, Int]] = {
//    var decoder: Reader = null
//    filename.substring(filename.lastIndexOf(".")) match {
//      case ".nt" => decoder = new FileReader(filename)
//      case ".gz" =>
//        val fileInputStream = new FileInputStream(filename)
//        val gzipStream = new GZIPInputStream(fileInputStream)
//        decoder = new InputStreamReader(gzipStream, "UTF-8")
//    }
//    var parser: RDFParser = null
//    format match {
//      case RDFFormat.TURTLE => parser = new TurtleParser()
//      case RDFFormat.NTRIPLES => parser = new NTriplesParser()
//      case RDFFormat.NQUADS => parser = new NQuadsParser()
//    }
//
//    //val handler = new StatementCounter()
//    //val handler = new RangeTypeConstraint()
//    //val handler = new CardinalityConstraint()
//
//    parser.setRDFHandler(handler)
//    // Configure the parser to be more error-tolerant.
//    // This allows to parse the 'good' triples in a noisy nquads/ntriples file.
//    // http://stackoverflow.com/questions/17628962/openrdf-sesame-is-it-possible-to-parse-single-lines
//    parser.getParserConfig.addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES)
//    parser.getParserConfig.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES)
//    parser.parse(decoder, "")
//
//    _log.info(s"${handler.countStatements} triples found in file '$filename'")
//
//    handler.getTransactions
//  }
//
//  /**
//   * For testing purposes.
//   * @return
//   */
//  def getSampleTransactions: Seq[Array[String]] = {
//    val transactions = Seq(
//      "dbp:name#count:3 dbp:address#count:5",
//      "dbp:name#count:3 dbp:address#count:5",
//      "dbp:name#count:3 dbp:address#count:4",
//      "dbp:name#count:2 dbp:address#count:4")
//      .map(_.split(" "))
//
//    transactions
//  }
//
//}
