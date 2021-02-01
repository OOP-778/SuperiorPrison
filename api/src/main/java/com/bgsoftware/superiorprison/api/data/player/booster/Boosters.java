package com.bgsoftware.superiorprison.api.data.player.booster;

import java.util.Optional;
import java.util.Set;

public interface Boosters {
  // Does prisoner have an active booster?
  boolean hasActiveBoosters();

  // Remove an booster
  void removeBooster(Booster booster);

  // Add a booster
  Booster addBooster(Class<? extends Booster> boosterClazz, long validTill, double rate);

  void addBooster(Booster booster);

  <T extends Booster> Set<T> findBoostersBy(Class<T> type);

  Optional<Booster> findBoosterBy(int id);

  Set<Booster> set();
}
