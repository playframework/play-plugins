package play.modules.statsd.api

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import play.Logger
import scala.util.Random
import play.api.Play

/**
 * Configuration trait for the [[play.modules.statsd.api.StatsdClient]].
 *
 * Provides to the client the prefix for all stats sent to statsd and mechanism
 * for sending stats over the network.
 */
trait StatsdClientCake {
  // Used as the prefix for all stats.
  protected val statPrefix: String

  // Used to actually send a stat to statsd.
  protected val send: Function1[String, Unit]

  // Used to time an operation.
  protected def now(): Long

  // Used to determine whether or not a sampled stat should be sent.
  protected def nextFloat(): Float
}

/**
 * Real implementation of [[play.modules.statsd.api.StatsdClientCake]].
 *
 * This implementation:
 * - Reads in values from `conf/application.conf`
 * - Sends stats using a `DatagramSocket` to statsd server.
 */
private[api] trait RealStatsdClientCake extends StatsdClientCake {

  // The property name for whether or not the statsd sending should be enabled.
  private val StatsdEnabledProperty = "statsd.enabled"

  // The property name for the statsd port.
  private val PortProperty = "statsd.port"

  // The property name for the statsd host.
  private val HostnameProperty = "statsd.host"

  // The property name for the application stat prefix.
  private val StatPrefixProperty = "statsd.stat.prefix"

  // Use scala's Random util for nextFloat.
  private lazy val random = new Random

  // The stat prefix used by the client.
  override val statPrefix = {
    Play.maybeApplication flatMap { _.configuration.getString(StatPrefixProperty) } getOrElse {
      Logger.warn("No stat prefix configured, using default of statsd")
      "statsd"
    }
  }

  /**
   * Use `System.currentTimeMillis()` to get the current time.
   */
  override def now(): Long = System.currentTimeMillis()

  /**
   * Use scala's [[scala.util.Random]] util for `nextFloat`.
   */
  override def nextFloat(): Float = random.nextFloat()

  /**
   * Expose a `send` function to the client. It is configured with the hostname and port.
   *
   * If statsd isn't enabled, it will be a noop function.
   */
  override lazy val send: Function1[String, Unit] = {
    try {
      // Check if Statsd sending is enabled.
      val enabled = please.booleanConfig(StatsdEnabledProperty)
      if (enabled) {
        // Initialize the socket, host, and port to be used to send the data.
        val socket = new DatagramSocket
        val hostname = please.config(HostnameProperty)
        val host = InetAddress.getByName(hostname)
        val port = please.intConfig(PortProperty)

        // Return the real send function, partially applied with the
        // socket, host, and port so the client only has to call "send(stat)".
        socketSend(socket, host, port) _
      } else {
        Logger.warn("Send will be NOOP because %s is not enabled".format(
          StatsdEnabledProperty))
        noopSend _
      }

    } catch {
      // If there is any error configuring the send function, log a warning
      // but don't throw an error. Use a noop function for all sends.
      case error: Throwable =>
        Logger.warn("Send will NOOP because of configuration problem.", error)
        noopSend _
    }
  }

  /**
   * Send the stat in a [[java.net.DatagramPacket]] to statsd.
   */
  private def socketSend(
    socket: DatagramSocket, host: InetAddress, port: Int)(stat: String) {
    try {
      val data = stat.getBytes
      socket.send(new DatagramPacket(data, data.length, host, port))
    } catch {
      case error: Throwable => Logger.error("", error)
    }
  }

  /**
   * Don't do anything. Used if statsd isn't enabled or on config errors.
   */
  private def noopSend(stat: String) = Unit
}
