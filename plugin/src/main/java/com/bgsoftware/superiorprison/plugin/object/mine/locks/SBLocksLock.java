package com.bgsoftware.superiorprison.plugin.object.mine.locks;

import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public class SBLocksLock implements Lock {
  private final UUID uuid = UUID.randomUUID();
  private final long lockedAt = System.currentTimeMillis();
  public Set<Location> lockedLocations = new HashSet<>();

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

    SBLocksLock that = (SBLocksLock) o;
    return Objects.equals(uuid, that.uuid);
  }

  @Override
  public int hashCode() {
    return uuid.hashCode();
  }
}
