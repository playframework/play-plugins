package play.modules.statsd.api
import play.api.Play
import play.api.Application

/**
 * Sugar to make using Java API for Play nicer.
 */
object please {
  private[api] def config(name: String): String = {
    Play.current.configuration.getString(name) getOrElse {
      throw new IllegalStateException("[%s] prop is null".format(name))
    }
  }

  private[api] def booleanConfig(name: String): Boolean = {
    // Use maybe application, so when we check if statsd is enabled, if there's no current application, no worries
    Play.maybeApplication.flatMap { _.configuration.getBoolean(name) } getOrElse false
  }

  private[api] def intConfig(name: String): Int = {
    Integer.parseInt(config(name))
  }
}