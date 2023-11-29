package example

import org.slf4j.LoggerFactory
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class RateLimitHandler {
  private val logger = LoggerFactory.getLogger(getClass)
  val RateLimitPause: FiniteDuration = 1.second
  var exponentialBackoff: FiniteDuration = 1.second

  def handle(): Unit = {
    logger.warn(s"Rate limited by Reddit. Waiting for $exponentialBackoff and then retrying...")
    Thread.sleep(exponentialBackoff.toMillis)
    exponentialBackoff *= 2
  }
}

