package com.bgsoftware.superiorprison.plugin.object.statistic;

import com.bgsoftware.superiorprison.api.data.statistic.StatisticsContainer;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.google.common.collect.Sets;
import com.oop.datamodule.DataBody;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public class SStatisticsContainer implements StatisticsContainer, DataBody {

    @Getter
    private UUID uuid;

    private SStatisticsContainer() {}

    public SStatisticsContainer(UUID uuid) {
        this.uuid = uuid;
    }

    @Getter
    private SBlocksStatistic blocksStatistic = new SBlocksStatistic();

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
    public void remove(boolean b) {
        new OTask()
                .sync(!b)
                .runnable(() -> SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SStatisticHolder.class).remove(this))
                .execute();
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
    public void save(boolean b) {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SStatisticHolder.class).save(this, b);
    }
}
