package com.bgsoftware.superiorprison.plugin.object.statistic;

import com.bgsoftware.superiorprison.api.data.statistic.BlocksStatistic;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SBlocksStatistic implements BlocksStatistic, Attachable<SStatisticContainer> {

    private transient SStatisticContainer container;

    @SerializedName(value = "minedBlocks")
    private Map<OMaterial, Long> minedBlocks = Maps.newConcurrentMap();

    @Override
    public void update(Material material, byte data, long amount) {
        update(OMaterial.matchMaterial(new ItemStack(material, 1, data)), amount);
    }

    public void update(OMaterial material, long amount) {
        minedBlocks.merge(material, amount, Long::sum);
        container.save(true);
    }

    @Override
    public long getTotal() {
        return minedBlocks.values().stream().mapToLong(value -> value).sum();
    }

    @Override
    public long get(Material material, byte data) {
        return get(OMaterial.matchMaterial(new ItemStack(material, 1, data)));
    }

    public long get(OMaterial material) {
        return minedBlocks.getOrDefault(material, 0L);
    }

    @Override
    public void attach(SStatisticContainer obj) {
        this.container = obj;
    }
}
