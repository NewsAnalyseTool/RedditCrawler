package example

import scala.collection.mutable
import scala.io.Source

class Environment {
  def load(): mutable.Map[String, String] = {
    val env = mutable.Map[String, String]()
    for (line <- Source.fromFile(".env").getLines()) {
      line.split("=").map(_.trim) match {
        case Array(key, value) =>
          env(key) = value.replaceAll("\"", "") // Remove double quotes
        case _ =>
          println(s"Warning: Skipping malformed line in .env: $line")
      }
    }
    env
  }
}
