package play.modules.statsd.api

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.concurrent.TrieMap
import java.util.Locale
import util.control.NonFatal

/**
 * Filter for reporting request metrics to statsd.  Usage:
 *
 * {{{
 * object Global extends WithFilters(new StatsdFilter()) {
 *   ...
 * }
 * }}}
 */
class StatsdFilter extends EssentialFilter {

  lazy val prefix = loadPrefix("statsd.routes.prefix", "routes.")
  lazy val totalPrefix = loadPrefix("statsd.routes.combined.prefix", "routes.combined.")

  def loadPrefix(key: String, default: String) = (for {
    app    <- Play.maybeApplication
    prefix <- app.configuration.getString(key)
  } yield {
    if (prefix.length() == 0) prefix else prefix + "."
  }).getOrElse(default)

  lazy val pathSeparator: Char = (for {
    app    <- Play.maybeApplication
    pathSeparatorString <- app.configuration.getString("statsd.routes.pathSeparator")
  } yield {
    if (pathSeparatorString.length != 1) {
        throw new IllegalStateException("pathSeparator [%s] is not a character".format(pathSeparatorString))
      }
      pathSeparatorString.charAt(0)
  }).getOrElse('.')

  def apply(next: EssentialAction) = new EssentialAction {
    def apply(rh: RequestHeader) = {

      val start = System.currentTimeMillis()

      // Calculate key
      val key = rh.tags.get(Routes.ROUTE_VERB).map({ verb =>
        val path = rh.tags(Routes.ROUTE_PATTERN)
        val cacheKey = verb + path
        prefix + keyCache.get(cacheKey).getOrElse {
          val key = statsKeyFromComments(rh.tags(Routes.ROUTE_COMMENTS)).getOrElse {
            // Convert paths of form GET /foo/bar/$paramname<regexp>/blah to foo.bar.paramname.blah.get
            val p = path.replaceAll("""\$([^<]+)<[^>]+>""", "$1").replace('/', pathSeparator).dropWhile(_ == pathSeparator)
            val normalisedPath = if (p.lastOption.filter(_ != '.').isDefined) p + '.' else p
            normalisedPath + verb.toLowerCase(Locale.ENGLISH)
          }
          keyCache.putIfAbsent(cacheKey, key)
          key
        }
      }).getOrElse(totalPrefix + "handlerNotFound")

      Statsd.increment(key)

      def handleError = {
        val time = System.currentTimeMillis() - start
        Statsd.timing(key, time)
        Statsd.timing(totalPrefix + "time", time)
        Statsd.increment(totalPrefix + "500")
        Statsd.increment(totalPrefix + "error")
      }

      def recordStats(result: Result) = {
        val time = System.currentTimeMillis() - start
        Statsd.timing(key, time)
        Statsd.timing(totalPrefix + "time", time)
        val status = result.header.status
        Statsd.increment(totalPrefix + status)
        if (status >= 500) {
          Statsd.increment(totalPrefix + "error")
        } else {
          Statsd.increment(totalPrefix + "success")
        }
        result
      }

      // Invoke the action
      try {
        next(rh).map(recordStats)
      } catch {
        case NonFatal(t) => {
          handleError
          throw t
        }
      }
    }
  }

  val StatsdKey = """.*@statsd\.key[ \t]+([^\s@]+).*""".r

  def statsKeyFromComments(comments: String): Option[String] = {
    comments match {
      case StatsdKey(key) => Some(key)
      case _ => None
    }
  }

  val keyCache = TrieMap.empty[String, String]

}
