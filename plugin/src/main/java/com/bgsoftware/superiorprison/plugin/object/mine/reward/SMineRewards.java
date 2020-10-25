package com.bgsoftware.superiorprison.plugin.object.mine.reward;

import com.bgsoftware.superiorprison.api.data.mine.reward.MineReward;
import com.bgsoftware.superiorprison.api.data.mine.reward.MineRewards;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SMineRewards implements SerializableObject, MineRewards, LinkableObject<SMineRewards> {
    private List<MineReward> rewards = new LinkedList<>();

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("rewards", rewards);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        serializedData
                .applyAsCollection("rewards")
                .forEach(it -> rewards.add(it.applyAs(SMineReward.class)));
    }

    @Override
    public List<MineReward> getRewards() {
        return Collections.unmodifiableList(rewards);
    }

    @Override
    public MineReward createReward(double chance, List<String> commands) {
        SMineReward reward = new SMineReward(chance, commands);
        rewards.add(reward);

        return reward;
    }

    @Override
    public void addReward(MineReward reward) {
        rewards.add(reward);
    }

    @Override
    public void removeReward(MineReward reward) {
        rewards.remove(reward);
    }

    @Override
    public void onChange(SMineRewards from) {
        this.rewards = from.rewards;
    }

    @Override
    public String getLinkId() {
        return "rewards";
    }
}
