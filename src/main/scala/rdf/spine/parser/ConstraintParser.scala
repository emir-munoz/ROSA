package rdf.spine.parser

import org.openrdf.rio.helpers.AbstractRDFHandler
import rdf.spine.model.Projection

/**
  * Trait to be respected by all constraints.
  *
  * @author Emir Munoz
  * @since 28/10/15.
  */
trait ConstraintParser extends AbstractRDFHandler {

  /**
    * @return All transactions found in the database.
    */
  //def getTransactions: Seq[Array[String]]
  def getTransactions: Seq[Projection]

  /**
    * @return Total number of statements.
    */
  def countStatements: Int

  /**
    * @return Total number of resources.
    */
  def countResources: Int

}
