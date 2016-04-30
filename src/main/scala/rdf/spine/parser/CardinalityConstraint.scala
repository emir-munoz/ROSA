package rdf.spine.parser

import org.openrdf.model.Statement
import rdf.spine.model.Projection

import scala.collection.mutable

/**
  * Extract transactions as for extracting CardinalityConstraint.
  *
  * @author Emir Munoz
  * @since 09/10/15.
  */
class CardinalityConstraint extends ConstraintParser {

  var transactions = mutable.Map[String, mutable.Map[String, Int]]()
  var statementsCount = 0

  override def handleStatement(st: Statement): Unit = {
    statementsCount += 1
    addTrans(st.getSubject.toString, st.getPredicate.toString)
  }

  /**
    * Add transaction based on a pair (subject, predicate) found in data.
    *
    * @param sub  Subject URI.
    * @param pred Predicate URI.
    */
  private def addTrans(sub: String, pred: String) = {
    transactions get sub match {
      case Some(a) => // if in map already, add item to the itemset
        //println(valu.toString)
        val predMap = transactions.getOrElseUpdate(sub, mutable.Map())
        //println(key + "=" + predMap)
        predMap get pred match {
          case Some(b) =>
            predMap.update(pred, predMap.get(pred).get + 1)
          case None =>
            predMap.put(pred, 1) // initialize the predicate
        }
        transactions.update(sub, predMap)
      case None => transactions.put(sub, mutable.Map((pred, 1))) // otherwise, add the value
    }
  }

  /**
    * Handler for comments.
    *
    * @param cm Comment.
    */
  override def handleComment(cm: String): Unit = {
  }

  /**
    * Handler for namespaces.
    *
    * @param prefix Namespace prefix.
    * @param uri    Namespace URI.
    */
  override def handleNamespace(prefix: String, uri: String) = {
  }

  /**
    * Get a sequence with all transactions found in data.
    *
    * @return All transactions found in the database.
    */
  def getTransactions: Seq[Projection] = {
    val projectionSet = mutable.Set[Projection]()

    transactions.foreach { trans =>
      val tResource = trans._1
      val tFreq: mutable.Map[String, Int] = trans._2
      val tFreqAux = tFreq.groupBy(_._2).mapValues(_.map(_._1))

      tFreqAux.foreach { item =>
        //println(x)
        val proj = new Projection(Set(item._1), Set(tResource), item._2.toSet)
        // println(proj)
        projectionSet.add(proj)
      }
    }

    projectionSet.toSeq
  }

  def countStatements: Int = {
    statementsCount
  }

  def countResources: Int = {
    transactions.keySet.size
  }

}
