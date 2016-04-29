package rdf.spine

import java.io.File

import spine.conf.Config

/**
  * @author Emir Munoz
  * @since 16/12/15.
  */
object Main {

  /**
    * Main method.
    * @param args Params
    */
  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("scopt", "3.x")
      opt[Int]('f', "foo") action { (x, c) =>
        c.copy(foo = x)
      } text ("foo is an integer property")
//      opt[File]('o', "out") required() valueName ("<file>") action { (x, c) =>
//        c.copy(out = x)
//      } text ("out is a required file property")
//      opt[(String, Int)]("max") action { case ((k, v), c) =>
//        c.copy(libName = k, maxCount = v)
//      } validate { x =>
//        if (x._2 > 0) success else failure("Value <max> must be >0")
//      } keyValueName("<libname>", "<max>") text ("maximum count for <libname>")
//      opt[Map[String, String]]("kwargs") valueName ("k1=v1,k2=v2...") action { (x, c) =>
//        c.copy(kwargs = x)
//      } text ("other arguments")
//      opt[Unit]("verbose") action { (_, c) =>
//        c.copy(verbose = true)
//      } text ("verbose is a flag")
//      help("help") text ("prints this usage text")
//      cmd("update") action { (_, c) =>
//        c.copy(mode = "update")
//      } text ("update is a command.") children(
//        opt[Unit]("not-keepalive") abbr ("nk") action { (_, c) =>
//          c.copy(keepalive = false)
//        } text ("disable keepalive"),
//        opt[Boolean]("xyz") action { (x, c) =>
//          c.copy(xyz = x)
//        } text ("xyz is a boolean property"),
//        checkConfig { c =>
//          if (c.keepalive && c.xyz) failure("xyz cannot keep alive") else success
//        }
//        )
    }
    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
      case Some(config) =>
        // do stuff
        println(config.foo)

      case None =>
        // arguments are bad, error message will have been displayed
    }


  }

}
