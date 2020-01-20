package com.bgsoftware.superiorprison.api.data.mine.settings;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public interface ResetSettings {

    default boolean isTimed() {
        return getClass().isAssignableFrom(Timed.class);
    }

    default Timed asTimed() {
        return isTimed() ? (Timed) this : Objects.requireNonNull(null, "Tried to get reset settings as Timed, but it's not instance of Timed.");
    }

    default Percentage asPercentage() {
        return !isTimed() ? (Percentage) this : Objects.requireNonNull(null, "Tried to get reset settings as Percentage, but it's not instance of Percentage.");
    }

    public static enum Type {

        PERCENTAGE,
        TIMED

    }

    public static class Timed {

        private int interval = -1;
        private TimeUnit unit = TimeUnit.SECONDS;

        public int getInterval() {
            return interval;
        }

        public TimeUnit getUnit() {
            return unit;
        }
    }

    public static class Percentage {

        private int requiredPercentage = -1;

        public int getRequiredPercentage() {
            return requiredPercentage;
        }
    }

}
