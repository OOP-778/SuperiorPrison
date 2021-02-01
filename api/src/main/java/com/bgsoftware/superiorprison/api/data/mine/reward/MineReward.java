package com.bgsoftware.superiorprison.api.data.mine.reward;

import java.util.List;

public interface MineReward {
  // Get mutable commands of the rewards
  List<String> getCommands();

  // Get chance of the reward
  double getChance();

  // Set chance of the reward
  void setChance(double chance);
}
