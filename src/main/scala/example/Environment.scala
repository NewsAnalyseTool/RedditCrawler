package example

import scala.collection.mutable
import scala.io.Source

/**
 * Loads environment variables from a `.env` file into a mutable map.
 *
 * Parses lines in the format `KEY=value`, ignoring malformatted lines.
 */
class Environment {
  /**
   * Reads `.env` file and populates a map with key-value pairs.
   *
   * Skips lines without `=` or improperly formatted. Removes surrounding quotes from values.
   *
   * @return A map of environment variables.
   */
  def load(): mutable.Map[String, String] = {
    val env = mutable.Map[String, String]()
    Source.fromFile(".env").getLines().foreach { line =>
      line.split("=", 2) match {
        case Array(key, value) => env(key.trim) = value.trim.replaceAll("^\"|\"$", "")
        case _ => println(s"Warning: Skipping malformed line: $line")
      }
    }
    env
  }
}
