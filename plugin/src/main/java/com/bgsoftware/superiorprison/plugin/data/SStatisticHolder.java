package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.StatisticsController;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.google.common.collect.Maps;
import com.oop.datamodule.storage.SqlStorage;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class SStatisticHolder extends SqlStorage<SStatisticsContainer> implements StatisticsController {

    private final Map<UUID, SStatisticsContainer> containers = Maps.newConcurrentMap();

    public SStatisticHolder(DatabaseController databaseController) {
        super(databaseController, databaseController.getDatabase());
    }

    @Override
    public Stream<SStatisticsContainer> stream() {
        return containers.values().stream();
    }

    @Override
    public Class<? extends SStatisticsContainer>[] getVariants() {
        return new Class[]{SStatisticsContainer.class};
    }

    @Override
    public void onAdd(SStatisticsContainer sStatisticContainer) {
        containers.put(sStatisticContainer.getUuid(), sStatisticContainer);
    }

    @Override
    public void onRemove(SStatisticsContainer sStatisticContainer) {
        containers.remove(sStatisticContainer);
    }

    @Override
    public SStatisticsContainer getContainer(UUID uuid) {
        SStatisticsContainer container = containers.get(uuid);
        if (container == null) {
            container = new SStatisticsContainer(uuid);
            containers.put(uuid, container);
            save(container, true);
        }

        return container;
    }

    @Override
    public Iterator<SStatisticsContainer> iterator() {
        return containers.values().iterator();
    }
}
