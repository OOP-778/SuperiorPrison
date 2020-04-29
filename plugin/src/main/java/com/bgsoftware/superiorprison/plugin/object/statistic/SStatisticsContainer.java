package com.bgsoftware.superiorprison.plugin.object.statistic;

import com.bgsoftware.superiorprison.api.data.statistic.StatisticsContainer;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.google.common.collect.Sets;
import com.oop.datamodule.DataBody;
import com.oop.datamodule.SerializedData;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public class SStatisticsContainer implements StatisticsContainer, DataBody {

    @Getter
    private UUID uuid;
    @Getter
    private SBlocksStatistic blocksStatistic = new SBlocksStatistic();

    private SStatisticsContainer() {
    }

    public SStatisticsContainer(UUID uuid) {
        this.uuid = uuid;
    }

    public Set<SStatistic> getAllStatistics() {
        return Sets.newHashSet(blocksStatistic);
    }

    @Override
    public String getTable() {
        return "statistics";
    }

    @Override
    public String getPrimaryKey() {
        return uuid.toString();
    }

    @Override
    public String[] getStructure() {
        return new String[]{
                "uuid",
                "blocks"
        };
    }

    @Override
    public void remove() {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SStatisticHolder.class).remove(this);
    }

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("uuid", uuid);
        serializedData.write("blocks", blocksStatistic);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.uuid = serializedData.applyAs("uuid", UUID.class);
        this.blocksStatistic = serializedData.applyAs("blocks", SBlocksStatistic.class);
        blocksStatistic.attach(this);
    }

    @Override
    public void save(boolean b, Runnable runnable) {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SStatisticHolder.class).save(this, b, runnable);
    }
}
