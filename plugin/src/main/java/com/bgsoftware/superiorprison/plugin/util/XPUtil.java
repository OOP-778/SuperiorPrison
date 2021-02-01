package com.bgsoftware.superiorprison.plugin.util;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;

public class XPUtil {
  public static void setTotalExperience(final Player player, final int exp) {
    Preconditions.checkArgument(exp > 0, "Cannot set the players xp to a negative value!");

    if (exp < 0) {
      throw new IllegalArgumentException("Experience is negative!");
    }
    player.setExp(0);
    player.setLevel(0);
    player.setTotalExperience(0);

    int amount = exp;
    while (amount > 0) {
      final int expToLevel = getExpAtLevel(player);
      amount -= expToLevel;
      if (amount >= 0) {
        player.giveExp(expToLevel);
      } else {
        amount += expToLevel;
        player.giveExp(amount);
        amount = 0;
      }
    }
  }

  private static int getExpAtLevel(final Player player) {
    return getExpAtLevel(player.getLevel());
  }

  public static int getExpAtLevel(final int level) {
    if (level <= 15) {
      return (2 * level) + 7;
    }
    if ((level >= 16) && (level <= 30)) {
      return (5 * level) - 38;
    }
    return (9 * level) - 158;
  }

  public static int getExpToLevel(final int level) {
    int currentLevel = 0;
    int exp = 0;

    while (currentLevel < level) {
      exp += getExpAtLevel(currentLevel);
      currentLevel++;
    }
    if (exp < 0) {
      exp = Integer.MAX_VALUE;
    }
    return exp;
  }

  public static int getTotalExperience(final Player player) {
    return Math.round(getExpAtLevel(player) * player.getExp())
        + getTotalExperience(player.getLevel());
  }

  public static int getTotalExperience(int currentLevel) {
    int exp = 0;
    while (currentLevel > 0) {
      currentLevel--;
      exp += getExpAtLevel(currentLevel);
    }
    if (exp < 0) {
      exp = Integer.MAX_VALUE;
    }
    return exp;
  }

  public static int getExpUntilNextLevel(final Player player) {
    int exp = Math.round(getExpAtLevel(player) * player.getExp());
    int nextLevel = player.getLevel();
    return getExpAtLevel(nextLevel) - exp;
  }
}
