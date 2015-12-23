/**
  * @author emir
  * @since 16/12/15.
  */
object ReverseMapTest {

  val m = Map(1 -> "a", 2 -> "b", 4 -> "b")
  m.groupBy(_._2).mapValues(_.map(_._1))

}
