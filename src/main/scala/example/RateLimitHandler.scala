package example

import java.lang.Thread.sleep
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class RateLimitHandler {
  val RateLimitPause: FiniteDuration = 1.second
  var exponentialBackoff: FiniteDuration = 1.second

  def handle(): Unit = {
    println(s"Rate limited by Reddit. Waiting for $exponentialBackoff and then retrying...")
    sleep(exponentialBackoff.toMillis)
    exponentialBackoff *= 2
  }
}
