package com.bgsoftware.superiorprison.api.data.mine.reward;

import java.util.List;
import java.util.Map;

public interface MineRewards {
    // Returns immutable copy of the rewards
    List<MineReward> getRewards();

    // Creates new reward
    MineReward createReward(double chance, List<String> commands);

    // Add a reward
    void addReward(MineReward reward);

    // Remove a reward
    void removeReward(MineReward reward);
}
