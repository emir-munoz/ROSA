//package spine.parser
//
//import org.openrdf.model.Statement
//import org.openrdf.model.impl.{SimpleBNode, SimpleIRI, SimpleLiteral}
//import org.openrdf.rio.helpers.AbstractRDFHandler
//
//import scala.collection.mutable
//
///**
// * Extract transactions as for extracting RangeTypeConstraint.
// *
// * @author Emir Munoz
// * @since 09/10/15.
// */
//class RangeTypeConstraint extends Constraint {
//
//  var transactions = mutable.Map[String, mutable.Set[String]]()
//  var statementsCount = 0
//
//  override def handleStatement(st: Statement): Unit = {
//    statementsCount += 1
//    st.getObject match {
//      case iri: SimpleIRI => {
//        //println(iri.toString)
//        //println(st.getPredicate + "###" + iri.getClass.getSimpleName)
//        addTrans(st.getSubject.toString, "%s###%s".format(st.getPredicate, iri.getClass.getSimpleName))
//      }
//      case lit: SimpleLiteral => {
//        //println(lit.getLabel)
//        //println(st.getObject.asInstanceOf[Literal].getDatatype)
//        //println(st.getPredicate + "###" + lit.getClass.getSimpleName)
//        addTrans(st.getSubject.toString, "%s###%s".format(st.getPredicate, lit.getClass.getSimpleName))
//      }
//      case bno: SimpleBNode => {
//        //println(bno.getID)
//        //println(st.getPredicate + "###" + bno.getClass.getSimpleName)
//        addTrans(st.getSubject.toString, "%s###%s".format(st.getPredicate, bno.getClass.getSimpleName))
//      }
//    }
//  }
//
//  /**
//   * Handler for comments.
//   * @param cm Comment.
//   */
//  override def handleComment(cm: String): Unit = {
//  }
//
//  /**
//   * Handler for namespaces.
//   * @param prefix Namespace prefix.
//   * @param uri Namespace URI.
//   */
//  override def handleNamespace(prefix: String, uri: String) = {
//  }
//
//  private def addTrans(key: String, value: String) = {
//    transactions get (key) match {
//      case Some(valu) => // if in map already, add item to the itemset
//        //println(valu.toString)
//        transactions.put(key, transactions.getOrElse(key, mutable.Set()) += value)
//      //        val types: ArrayBuffer[String] = transactions.get(key).get
//      //        if (!types.contains(value)) {
//      //          types.append(value)
//      //          transactions.put(key, types)
//      //        }
//      case None => transactions.put(key, mutable.Set(value)) // otherwise, add the value
//    }
//  }
//
//  def getTransactions: Seq[Array[String]] = {
//    transactions.values.map(x => x.toArray).toArray.toSeq
//  }
//
//  def countStatements: Int = {
//    statementsCount
//  }
//
//  def countResources: Int = {
//    transactions.keySet.size
//  }
//
//}
