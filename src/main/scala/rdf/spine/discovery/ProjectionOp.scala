package rdf.spine.discovery

import java.io._
import java.util.zip.GZIPInputStream

import org.openrdf.rio.helpers.{BasicParserSettings, NTriplesParserSettings}
import org.openrdf.rio.nquads.NQuadsParser
import org.openrdf.rio.ntriples.NTriplesParser
import org.openrdf.rio.turtle.TurtleParser
import org.openrdf.rio.{RDFFormat, RDFParser}
import org.slf4j.LoggerFactory
import spine.model.Projection
import spine.parser.Constraint

import scala.collection.mutable

/**
  * Projection Operator over an RDF knowledge graph.
  *
  * @author Emir Munoz
  * @since 08/10/15.
  * @version 0.0.1
  */
object ProjectionOp {

  val _log = LoggerFactory.getLogger(ProjectionOp.getClass.getSimpleName)
  var numSubjects = 0

  /**
    *
    * @param filename Path to the RDF file.
    * @param format   Format of the RDF triples.
    * @param handler  Handler for the parser.
    * @return Sequence of properties and frequencies per subject.
    */
  //Seq[Array[String]]
  //mutable.Map[String, mutable.Map[String, Int]]
  def getProjections(filename: String, format: RDFFormat, handler: Constraint): Seq[Projection] = {
    var decoder: Reader = null
    filename.substring(filename.lastIndexOf(".")) match {
      case ".nt" => decoder = new FileReader(filename)
      case ".gz" =>
        val fileInputStream = new FileInputStream(filename)
        val gzipStream = new GZIPInputStream(fileInputStream)
        decoder = new InputStreamReader(gzipStream, "UTF-8")
    }
    var parser: RDFParser = null
    format match {
      case RDFFormat.TURTLE => parser = new TurtleParser()
      case RDFFormat.NTRIPLES => parser = new NTriplesParser()
      case RDFFormat.NQUADS => parser = new NQuadsParser()
    }

    parser.setRDFHandler(handler)
    // Configure the parser to be more error-tolerant.
    // This allows to parse the 'good' triples in a noisy nquads/ntriples file.
    // http://stackoverflow.com/questions/17628962/openrdf-sesame-is-it-possible-to-parse-single-lines
    parser.getParserConfig.addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES)
    parser.getParserConfig.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES)
    parser.parse(decoder, "")

    _log.info(s"${handler.countStatements} RDF triples found in file '$filename'")
    _log.info(s"${handler.countResources} different subjects found in file '$filename'")

    numSubjects = handler.countResources

    handler.getTransactions
  }

  def breakProjection(proj: Projection): Seq[(String, Projection)] = {
    val res = mutable.Map[String, Projection]()
    proj.properties.foreach { prop =>
      res.put(prop.toString, new Projection(proj.freq, proj.subjects, Set(prop)))
      //res.add(new Projection(proj.freq, proj.subjects, Set(prop)))
    }

    //println(res.mkString(";"))

    res.toSeq
  }

  def getNumSubjects: Int = {
    numSubjects
  }

  /**
    * For testing purposes.
    *
    * @return
    */
  def getSampleTransactions: Seq[Array[String]] = {
    val transactions = Seq(
      "dbp:name#count:3 dbp:address#count:5",
      "dbp:name#count:3 dbp:address#count:5",
      "dbp:name#count:3 dbp:address#count:4",
      "dbp:name#count:2 dbp:address#count:4")
      .map(_.split(" "))

    transactions
  }

}
