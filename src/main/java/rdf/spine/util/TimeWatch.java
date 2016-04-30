package rdf.spine.util;

import java.util.concurrent.TimeUnit;

/**
 * Time watch.
 *
 * @author Emir Munoz
 * @since 28/04/16.
 * @version 0.0.2
 */
public class TimeWatch {

    private long _starts;

    private TimeWatch() {
        reset();
    }

    public static TimeWatch start() {
        return new TimeWatch();
    }

    public TimeWatch reset() {
        _starts = System.currentTimeMillis();
        return this;
    }

    public long time() {
        long _ends = System.currentTimeMillis();
        return _ends - _starts;
    }

    public long time(TimeUnit unit) {
        return unit.convert(time(), TimeUnit.MILLISECONDS);
    }

}
