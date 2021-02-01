package com.bgsoftware.superiorprison.plugin.object.mine.locks;

import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import java.util.Objects;
import java.util.UUID;

public class SMineLock implements Lock {
  private final UUID uuid = UUID.randomUUID();
  private final long lockedAt = System.currentTimeMillis();

  @Override
  public UUID getUUID() {
    return uuid;
  }

  @Override
  public long getLockedAt() {
    return lockedAt;
  }

  @Override
  public long getLockedFor() {
    return System.currentTimeMillis() - lockedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SMineLock sMineLock = (SMineLock) o;

    return Objects.equals(uuid, sMineLock.uuid);
  }

  @Override
  public int hashCode() {
    return uuid.hashCode();
  }
}
