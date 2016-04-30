package rdf.spine.discovery

import rdf.spine.model.Projection

/**
  * JOIN Operator implementation.
  *
  * @author Emir Munoz
  * @since 16/12/15.
  */
object JoinOp {

  /**
    * Join operator between two projections.
    *
    * @param left  A projection.
    * @param right A projection.
    * @return A join of the given projections.
    */
  def join(left: Projection, right: Projection): Seq[Projection] = {
    val res: Array[Projection] = Array()

    val intersection = left.properties intersect right.properties
    val leftBigger = left.properties diff intersection
    val rightBigger = right.properties diff intersection

    if (intersection nonEmpty) {
      val proj = new Projection(left.freq union right.freq, left.subjects union right.subjects, intersection)
      res :+ proj
    } else if (leftBigger nonEmpty) {
      val proj = new Projection(left.freq, left.subjects, leftBigger)
      res :+ proj
    } else if (rightBigger nonEmpty) {
      val proj = new Projection(right.freq, right.subjects, rightBigger)
      res :+ proj
    } else {
      // empty set
    }

    res.toSeq
  }

  /**
    * Specific Join operator for two projections with the same set of properties.
    *
    * @param left  A projection.
    * @param right A projection.
    * @return A join of the given projections.
    */
  def joinSingle(left: Projection, right: Projection): Projection = {

    //println(left + " **JOIN** " + right)

    val intersection = left.properties intersect right.properties
    // val leftBigger = left.properties diff intersection
    // val rightBigger = right.properties diff intersection

    if (intersection nonEmpty) {
      //println("Intersection is non empty: " + intersection)
      return new Projection(left.freq union right.freq, left.subjects union right.subjects, left.properties union right.properties)
    }

    null // different set of properties
  }

}
