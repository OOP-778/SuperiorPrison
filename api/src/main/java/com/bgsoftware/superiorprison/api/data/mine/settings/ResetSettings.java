package com.bgsoftware.superiorprison.api.data.mine.settings;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public interface ResetSettings {

    ResetType getType();

    long getValue();

    void setValue(long value);

    String getValueHumanified();
    String getCurrentHumanified();

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

    public static interface Timed extends ResetSettings {
        ZonedDateTime getResetDate();
    }

    public static interface Percentage extends ResetSettings {
    }
}
