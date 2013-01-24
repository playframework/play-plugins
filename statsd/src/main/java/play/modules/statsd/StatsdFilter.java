package play.modules.statsd;

import play.api.mvc.EssentialAction;
import play.api.mvc.EssentialFilter;

/**
 * Filter for reporting request metrics to Statsd.
 *
 * Usage:
 *
 * <pre>
 *     public class Global extends GlobalSettings {
 *         public <T extends play.api.mvc.EssentialFilter> Class<T>[] filters() {
 *             return new Class[] {StatsdFilter.class};
 *         }
 *     }
 * </pre>
 */
public class StatsdFilter implements EssentialFilter {

    // We have to keep a static reference since the Java GlobalSettings instantiates a new filter for every request,
    // and the filter holds a cache.
    private static final play.modules.statsd.api.StatsdFilter filter = new play.modules.statsd.api.StatsdFilter();

    @Override
    public EssentialAction apply(EssentialAction next) {
        return filter.apply(next);
    }
}
