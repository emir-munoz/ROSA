package rdf.spine.conf

import java.io.File

/**
  * Config class for using Scopt.
  *
  * @author Emir Munoz
  * @since 16/12/15.
  */
case class Config(foo: Int = -1)

//, out: File = new File("."), xyz: Boolean = false,
//libName: String = "", maxCount: Int = -1, verbose: Boolean = false, debug: Boolean = false,
//mode: String = "", files: Seq[File] = Seq(), keepalive: Boolean = false,
//jars: Seq[File] = Seq(), kwargs: Map[String, String] = Map()
