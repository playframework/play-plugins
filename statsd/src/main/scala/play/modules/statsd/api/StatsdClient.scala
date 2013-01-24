package play.modules.statsd.api

import play.Logger

/**
 * Trait defining the statsd interface. It defines the two stats calls in
 * statsd: `increment` and `timing`. It must be instantiated with
 * [[play.modules.statsd.api.StatsdClientCake]] which handles the sending of stats over the network.
 *
 * Two stats-related function are supported:
 * - `increment`: Increment a given stat key.
 * - `timing`: Sending timing info for a given operation key.
 *
 * For both, an optional `samplingRate` parameter can be provided.
 * For parameters between 0 and 1.0, the client will send the stat
 * `samplingRate * 100` of the time. This is useful for some stats that
 * occur extremely frequently and therefore put too much load on the statsd
 * server.
 *
 * The functionality is exposed to Play Apps using the [[play.modules.statsd.Statsd]] object.
 */
trait StatsdClient {
  self: StatsdClientCake =>

  // Suffix for increment stats.
  private val IncrementSuffix = "c"

  // Suffix for timing stats.
  private val TimingSuffix = "ms"

  // Suffix for gauge stats.
  private val GaugeSuffix = "g"

  /**
   * Increment a given stat key. Optionally give it a value and sampling rate.
   *
   * @param key The stat key to be incremented.
   * @param value The amount by which to increment the stat. Defaults to 1.
   * @param samplingRate The probability for which to increment. Defaults to 1.
   */
  def increment(key: String, value: Long = 1, samplingRate: Double = 1.0) {
    safely { maybeSend(statFor(key, value, IncrementSuffix, samplingRate), samplingRate) }
  }

  /**
   * Timing data for given stat key. Optionally give it a sampling rate.
   *
   * @param key The stat key to be timed.
   * @param millis The number of milliseconds the operation took.
   * @param samplingRate The probability for which to increment. Defaults to 1.
   */
  def timing(key: String, millis: Long, samplingRate: Double = 1.0) {
    safely { maybeSend(statFor(key, millis, TimingSuffix, samplingRate), samplingRate) }
  }

  /**
   * Time a given operation and send the resulting stat.
   *
   * @param key The stat key to be timed.
   * @param samplingRate The probability for which to increment. Defaults to 1.
   * @param timed An arbitrary block of code to be timed.
   * @return The result of the timed operation.
   */
  def time[T](key: String, samplingRate: Double = 1.0)(timed: => T): T = {
    val start = now()
    val result = timed
    val finish = now()
    timing(key, finish - start, samplingRate)
    result
  }

  /**
   * Record the given value.
   *
   * @param key The stat key to update.
   * @param value The value to record for the stat.
   */
  def gauge(key: String, value: Long) {
    safely { maybeSend(statFor(key, value, GaugeSuffix, 1.0), 1.0) }
  }

  /*
   * ****************************************************************
   *                PRIVATE IMPLEMENTATION DETAILS
   * ****************************************************************
   */

  /*
   * Creates the stat string to send to statsd.
   * For counters, it provides something like {@code key:value|c}.
   * For timing, it provides something like {@code key:millis|ms}.
   * If sampling rate is less than 1, it provides something like {@code key:value|type|@rate}
   */
  private def statFor(key: String, value: Long, suffix: String, samplingRate: Double): String = {
    samplingRate match {
      case x if x >= 1.0 => "%s.%s:%s|%s".format(statPrefix, key, value, suffix)
      case _ => "%s.%s:%s|%s|@%f".format(statPrefix, key, value, suffix, samplingRate)
    }
  }

  /*
   * Probabilistically calls the {@code send} function. If the sampling rate
   * is 1.0 or greater, we always call send. Use a random number call send
   * function {@code (samplingRate * 100)%} of the time.
   */
  private def maybeSend(stat: String, samplingRate: Double) {
    if (samplingRate >= 1.0 || nextFloat() < samplingRate) {
      send(stat)
    }
  }

  /*
   * Safety net for operations that shouldn't throw exceptions.
   */
  private def safely(operation: => Unit) {
    try {
      operation
    } catch {
      case error => Logger.warn("Unhandled throwable sending stat.", error)
    }
  }
}

/**
 * Wrap the [[play.modules.statsd.api.StatsdClient]] trait configured with
 * [[play.modules.statsd.api.RealStatsdClientCake]] in an object to make it available to the app.
 */
object Statsd extends StatsdClient with RealStatsdClientCake
