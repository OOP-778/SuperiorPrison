package com.bgsoftware.superiorprison.plugin.object.statistic;

import com.bgsoftware.superiorprison.api.data.statistic.StatisticsContainer;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.holders.SStatisticHolder;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.Removeable;
import com.google.common.collect.Sets;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

public class SStatisticsContainer implements StatisticsContainer, UniversalBodyModel, Removeable {

    @Getter
    private UUID uuid;

    @Getter
    @Setter
    private boolean removed;

    @Getter
    private SBlocksStatistic blocksStatistic = new SBlocksStatistic();

    @Getter
    private SPrisoner cachedPrisoner;

    private SStatisticsContainer() {
    }

    public SStatisticsContainer(UUID uuid) {
        this.uuid = uuid;
        blocksStatistic.attach(this);
    }

    public SPrisoner getPrisoner() {
        if (cachedPrisoner == null)
            cachedPrisoner = (SPrisoner) SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(uuid).orElse(null);

        return cachedPrisoner;
    }

    public Set<SStatistic> getAllStatistics() {
        return Sets.newHashSet(blocksStatistic);
    }

    @Override
    public String getKey() {
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
        removed = true;
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

    @Override
    public String getIdentifierKey() {
        return "statisticsContainer";
    }
}
