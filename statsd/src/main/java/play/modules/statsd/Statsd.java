package play.modules.statsd;

import play.libs.F;
import play.modules.statsd.api.Statsd$;
import play.modules.statsd.api.StatsdClient;
import scala.runtime.AbstractFunction0;

/**
 * Java API to Statsd
 */
public class Statsd {

    /**
     * Increment the given key by 1
     *
     * @param key The key to increment
     */
    public static void increment(String key) {
        client().increment(key, 1, 1.0);
    }

    /**
     * Increment the given key by the given value
     *
     * @param key   The key to increment
     * @param value The value to increment it by
     */
    public static void increment(String key, long value) {
        client().increment(key, value, 1.0);
    }

    /**
     * Increment the given key by the given value at the given rate
     *
     * @param key   The key to increment
     * @param value The value to increment it by
     * @param rate  The rate to sample at
     */
    public static void increment(String key, long value, double rate) {
        client().increment(key, value, rate);
    }

    /**
     * Increment the given key by 1 at the given rate
     *
     * @param key  The key to increment
     * @param rate The rate to sample at
     */
    public static void increment(String key, double rate) {
        client().increment(key, 1, rate);
    }

    /**
     * Reporting timing for the given key
     *
     * @param key The key to report timing for
     * @param ms  The time to report
     */
    public static void timing(String key, long ms) {
        client().timing(key, ms, 1.0);
    }

    /**
     * Reporting timing for the given key at the given rate
     *
     * @param key  The key to report timing for
     * @param ms   The time to report
     * @param rate The rate to sample at
     */
    public static void timing(String key, long ms, double rate) {
        client().timing(key, ms, rate);
    }

    /**
     * Time the given function and report the timing on the given key
     *
     * @param key   The key to report timing for
     * @param timed The function to time
     */
    public static <T> T time(String key, F.Function0<T> timed) {
        return time(key, 1.0, timed);
    }

    /**
     * Time the given function and report the timing on the given key at the given rate
     *
     * @param key   The key to report timing for
     * @param rate  The rate to sample at
     * @param timed The function to time
     */
    public static <T> T time(String key, double rate, final F.Function0<T> timed) {
        return client().time(key, rate, new AbstractFunction0<T>() {
            public T apply() {
                try {
                    return timed.apply();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        });
    }

    /**
     * Record the given value.
     *
     * @param key The stat key to update.
     * @param value The value to record for the stat.
     */
    public static void gauge(String key, long value) {
        client().gauge(key, value);
    }

    private static StatsdClient client() {
        return Statsd$.MODULE$;
    }
}
