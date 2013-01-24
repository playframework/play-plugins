import play.api.mvc.WithFilters
import play.modules.statsd.api.StatsdFilter

object Global extends WithFilters(new StatsdFilter)
