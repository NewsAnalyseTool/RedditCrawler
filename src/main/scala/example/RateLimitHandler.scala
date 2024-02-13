package example

import org.slf4j.LoggerFactory
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * Manages API rate limiting by implementing an exponential backoff strategy.
 */
class RateLimitHandler {
  private val logger = LoggerFactory.getLogger(getClass)
  val RateLimitPause: FiniteDuration = 1.second
  var exponentialBackoff: FiniteDuration = 1.second

  /**
   * Handles rate limiting by pausing execution and doubling the wait time for subsequent retries.
   */
  def handle(): Unit = {
    logger.warn(s"Rate limited by Reddit. Waiting for $exponentialBackoff and then retrying...")
    Thread.sleep(exponentialBackoff.toMillis)
    exponentialBackoff *= 2
  }
}

