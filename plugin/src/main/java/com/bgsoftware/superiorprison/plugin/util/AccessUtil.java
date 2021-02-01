package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import java.util.Optional;

public class AccessUtil {
  public static Optional<Prestige> findPrestige(String name) {
    return SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestige(name);
  }

  public static Optional<Rank> findRank(String name) {
    return SuperiorPrisonPlugin.getInstance().getRankController().getRank(name);
  }
}
