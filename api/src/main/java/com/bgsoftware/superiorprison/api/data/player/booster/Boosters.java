package com.bgsoftware.superiorprison.api.data.player.booster;

import java.util.Optional;
import java.util.Set;

public interface Boosters {
    boolean hasActiveBoosters();

    void removeBooster(Booster booster);

    Booster addBooster(Class<? extends Booster> boosterClazz, long validTill, double rate);

    void addBooster(Booster booster);

    <T extends Booster> Set<T> findBoostersBy(Class<T> type);

    Optional<Booster> findBoosterBy(int id);

    Set<Booster> set();
}
