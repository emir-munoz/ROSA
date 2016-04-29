package rdf.spine.parser

import org.openrdf.model.{Literal, Statement}
import org.openrdf.model.impl.SimpleLiteral
import org.openrdf.rio.helpers.{AbstractRDFHandler, RDFHandlerBase}

/**
 * http://rdf4j.org/sesame/2.7/docs/articles/rio-parser/custom-handler.docbook?view
 *
 * @author Emir Munoz
 * @since 08/10/15.
 */
class StatementCounter extends AbstractRDFHandler {
  var countedStatements: Int = 0

  override def handleStatement(st: Statement): Unit = {
    countedStatements += 1
    if (st.getObject.isInstanceOf[SimpleLiteral]){
      println(st.getObject.asInstanceOf[Literal].getDatatype)
    }
    //println(st.getObject.getClass.getSimpleName)
  }

  override def handleComment(cm: String): Unit = {
    /** **/
  }

  def getCountedStatements(): Int = {
    countedStatements
  }

}
