package spine.model

/**
  * Class to represent a projection.
  *
  * @author Emir Munoz
  * @since 16/12/15.
  */
class Projection(var freq: Set[Int], var subjects: Set[String], var properties: Set[String]) extends Serializable {

  override def toString: String = {
    s"\\prod^{${freq.mkString(",")}}_{${subjects.mkString(",")}(${properties.mkString(",")})"
  }

}
