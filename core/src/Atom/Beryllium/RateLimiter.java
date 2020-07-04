package Atom.Beryllium;

import arc.Core;
import arc.func.Cons;

import java.time.Instant;


/** Simple rateLimiter */
public class RateLimiter {
    public int eventLimit = 10;
    public int findTime = 1000;
    public Instant begin = Instant.now();
    public int count = 0;

    private boolean noUpdate = false;

    public RateLimiter() {}

    /**
     * The constructor
     * @param eventLimit Event limit
     * @param findTime Time interval in milliseconds
     */
    public RateLimiter(int eventLimit, int findTime) {
        this.eventLimit = eventLimit;
        this.findTime = findTime;
    }

    /**
     * Helper to update begin time
     * @return True if in new interval, false otherwise
     */
    private void updateBegin() {
        if (Instant.now().isAfter(begin.plusMillis(findTime)) && !noUpdate) {
            // new interval
            begin = Instant.now();
            count = 0;
        }
    }

    /**
     * Check and update ratelimit
     * @return True if ratelimit exceeded, false otherwise
     */
    public boolean get() {
        updateBegin();
        count++;
        return count > eventLimit;
    }

    public boolean get(int eventLimit) {
        updateBegin();
        count++;
        return count > eventLimit;
    }

    /**
     * Check ratelimit
     * @return True if ratelimit exceeded, false otherwise
     */
    public boolean check() {
        updateBegin();
        return count > eventLimit;
    }

    /** Get number of events in current interval */
    public int events() {
        updateBegin();
        return count;
    }

    /**
     * Provide count next tick. Will inhibit reset
     * @param fn Function to be run
     */
    public void nextTick(Cons<RateLimiter> fn) {
        noUpdate = true;
        Core.app.post(() -> {
            fn.get(this);
            noUpdate = false;
        });
    }
}
