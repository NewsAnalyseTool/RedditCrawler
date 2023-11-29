package example

import scala.collection.mutable
import scala.io.Source

class Environment {
  def load(): mutable.Map[String, String] = {
    val env = mutable.Map[String, String]()
    for (line <- Source.fromFile(".env").getLines()) {
      val index = line.indexOf("=")
      if (index > 0) {
        val key = line.substring(0, index).trim
        val value = line.substring(index + 1).trim.replaceAll("^\"|\"$", "") // Remove leading and trailing double quotes
        env(key) = value
      } else {
        println(s"Warning: Skipping malformed line in .env: $line")
      }
    }
    env
  }
}

