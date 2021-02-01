package com.bgsoftware.superiorprison.api.data.mine.settings;

public interface MineSettings {

  /*
  Get how much players can be inside a mine
  */
  int getPlayerLimit();

  /*
  Get reset settings
  */
  ResetSettings getResetSettings();

  // Check if teleporting is enabled
  boolean isTeleportation();

  // Check if enderpearls are disabled
  boolean isDisableEnderPearls();

  // Check if animal spawn is disabled
  boolean isDisableAnimalSpawn();

  // Check if monster spawn is disabled
  boolean isDisableMonsterSpawn();
}
