package com.bgsoftware.superiorprison.api.data.mine.settings;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public interface ResetSettings {

    // Get reset type (either percentage or timed)
    ResetType getType();

    // Get value (if percentage it's gonna return percentage required if timed interval in seconds)
    long getValue();

    // Set value (If percentage a value between 0-100, if timed interval in seconds)
    void setValue(long value);

    // Get (percentage or interval) in human readable string (2h5m3s)
    String getValueHumanified();

    // Get current (percentage or time left) in human readable string (2m2s)
    String getCurrentHumanified();

    // If reset settings is timed
    default boolean isTimed() {
        return getType() == ResetType.TIMED;
    }

    default <T extends ResetSettings> T as(Class<T> type) {
        return getClass().isAssignableFrom(type) ? (T) this : Objects.requireNonNull(null, "Tried to get reset settings as " + type.getSimpleName() + ", but it's not instance of " + type.getSimpleName() + ".");
    }

    default Timed asTimed() {
        return isTimed() ? (Timed) this : Objects.requireNonNull(null, "Tried to get reset settings as Timed, but it's not instance of Timed.");
    }

    default Percentage asPercentage() {
        return !isTimed() ? (Percentage) this : Objects.requireNonNull(null, "Tried to get reset settings as Percentage, but it's not instance of Percentage.");
    }

    interface Timed extends ResetSettings {
        ZonedDateTime getResetDate();
    }

    interface Percentage extends ResetSettings {
    }
}
