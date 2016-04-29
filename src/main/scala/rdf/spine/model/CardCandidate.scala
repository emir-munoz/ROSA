package rdf.spine.model

/**
  * Class to represent a cardinality constraint candidate.
  *
  * @author Emir Munoz
  * @since 17/12/15.
  */
class CardCandidate(var minBound: Int, var maxBound: Int, var context: String, var properties: Set[String]) extends Serializable {

  def this() {
    this(0, 0, "", Set())
  }

  def this(minBound: Int, maxBound: Int, context: String) {
    this(minBound, maxBound, context, Set())
  }

  def this(proj: Projection, context: String, total: Int) {
    this()
    if (proj.subjects.size == total) {
      this.minBound = proj.freq.min
    } else {
      this.minBound = 0
    }
    this.maxBound = proj.freq.max
    this.context = context
    this.properties = proj.properties
  }

  def toShEx: String = {
    // dot represents anything, i.e., IRI, xsd:string, etc.
    s"<${properties.head}> .{$minBound, $maxBound} ,"
  }

  override def toString: String = {
    s"card(${properties.mkString("{", ",", "}")}, $context) = ($minBound, $maxBound)"
  }

}
