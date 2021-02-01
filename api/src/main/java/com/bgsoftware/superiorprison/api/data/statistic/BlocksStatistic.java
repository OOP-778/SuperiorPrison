package com.bgsoftware.superiorprison.api.data.statistic;

import org.bukkit.Material;

public interface BlocksStatistic {

  /**
   * Updates mined material amount
   *
   * @param material block material
   * @param data block data for legacy versions.
   * @param amount amount to add
   */
  void update(Material material, byte data, long amount);

  long getTotal();

  long get(Material material, byte data);
}
