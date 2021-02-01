package com.bgsoftware.superiorprison.api.data.mine.locks;

import java.util.UUID;

public interface Lock {
  // Get id of the lock
  UUID getUUID();

  // Get when the lock was initialized
  long getLockedAt();

  // Get for how it's locked for
  long getLockedFor();
}
