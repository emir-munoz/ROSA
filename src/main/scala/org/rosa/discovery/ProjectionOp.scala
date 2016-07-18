package rdf.rosa.discovery

import java.io._
import java.util.zip.GZIPInputStream

import org.openrdf.rio.helpers.{BasicParserSettings, NTriplesParserSettings}
import org.openrdf.rio.nquads.NQuadsParser
import org.openrdf.rio.ntriples.NTriplesParser
import org.openrdf.rio.turtle.TurtleParser
import org.openrdf.rio.{RDFFormat, RDFParser}
import org.slf4j.LoggerFactory
import rdf.rosa.model.Projection
import rdf.rosa.parser.ConstraintParser

import scala.collection.mutable

/**
  * Projection Operator over an RDF knowledge graph.
  *
  * @author Emir Munoz
  * @since 08/10/15.
  * @version 0.0.2
  */
object ProjectionOp {

  val _log = LoggerFactory.getLogger(ProjectionOp.getClass.getSimpleName)
  var numSubjects = 0

  /**
    * Extract projections from a given RDF file.
    *
    * @param filename Path to the RDF file.
    * @param format   Format of the RDF triples.
    * @param handler  Handler for the parser.
    * @return Sequence of properties and frequencies per subject.
    */
  def getProjections(filename: String, format: RDFFormat, handler: ConstraintParser): Seq[Projection] = {
    _log.info(s"Processing dataset file '$filename'")
    _log.info("Loading RDF statements in memory ...")
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

    _log.info(s"${handler.countStatements} RDF triples found in dataset")
    _log.info(s"${handler.countResources} different subjects found in dataset")

    numSubjects = handler.countResources

    handler.getTransactions
  }

  /**
    * Break coarse projections into small projections with single predicate.
    *
    * @param proj Coarse projection.
    * @return Fine projection.
    */
  def breakProjection(proj: Projection): Seq[(String, Projection)] = {
    val res = mutable.Map[String, Projection]()
    proj.properties.foreach { prop =>
      res.put(prop.toString, new Projection(proj.freq, proj.subjects, Set(prop)))
      //res.add(new Projection(proj.freq, proj.subjects, Set(prop)))
    }

    res.toSeq
  }

  /**
    * @return Number of subjects in dataset.
    */
  def getNumSubjects: Int = {
    numSubjects
  }

  /**
    * @return Sample transactions for testing purposes.
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
