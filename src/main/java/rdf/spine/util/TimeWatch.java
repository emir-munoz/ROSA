package rdf.spine.util;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author Emir Munoz
 * @since 28/04/16.
 */
public class TimeWatch {

    private Instant _start;

    private TimeWatch() {
        reset();
    }

    public static TimeWatch start() {
        return new TimeWatch();
    }

    public TimeWatch reset() {
        _start = Instant.now();
        return this;
    }

    public long stop() {
        Instant _stop = Instant.now();
        return Duration.between(_start, _stop).toMillis();
    }

    public long time(TimeUnit unit) {
        // TimeUnit.MILLISECONDS
        // unit.convert(time(), unit);
        return unit.toSeconds(stop());
    }

}
