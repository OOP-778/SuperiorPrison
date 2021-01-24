package com.bgsoftware.superiorprison.plugin.object.mine.reward;

import com.bgsoftware.superiorprison.api.data.mine.reward.MineReward;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SMineReward implements MineReward, SerializableObject {

    @Setter
    private double chance;
    private List<String> commands = new ArrayList<>();

    public SMineReward() {}

    public SMineReward(double chance, List<String> commands) {}

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("chance", chance);
        serializedData.write("commands", commands);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.chance = serializedData.applyAs("chance", double.class);
        this.commands = serializedData.applyAsCollection("commands")
                .map(c -> c.applyAs(String.class))
                .collect(Collectors.toList());
    }
}
