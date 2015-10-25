package canoe.timer;


/**
 * A time source in the spirit of {@link Ticker}. Implementations should return the number of nanoseconds
 * since some fixed but arbitrary point in time, just like System.nanoTime().
 * <p>
 * The obvious default implementation which uses {@link System#nanoTime()} is provided as {@link #SYSTEM_SOURCE}, and
 * that's likely all your need except for testing purposes.
 */
public abstract class NanoSource {

    /**
     * The <i>system</i> nano source, backed by {@link System#nanoTime()}.
     */
    public static final NanoSource SYSTEM_SOURCE = new NanoSource() {
        @Override
        public long getNanos() {
            return System.nanoTime();
        }
    };

    /**
     * Return the current time, relative to an arbitrary epoch, in nanoseconds.
     * <p>
     * This is only used to measure intervals, and hence doesn't have to
     * correspond to any notion of calendar time. In particular, this implies
     * that sources such as {@link System#nanoTime()} will work fine.
     */
    abstract public long getNanos();
}