package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.StatisticsController;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.oop.datamodule.universal.UniversalStorage;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SStatisticHolder extends UniversalStorage<SStatisticsContainer> implements StatisticsController {
    private Map<UUID, SStatisticsContainer> statisticsContainerMap = new ConcurrentHashMap<>();

    public SStatisticHolder(DatabaseController controller) {
        super(controller);

        addVariant("statistics", SStatisticsContainer.class);

        currentImplementation(
                SuperiorPrisonPlugin.getInstance().getMainConfig().getStorageSection().provideFor(this, "statistics")
        );
    }

    @Override
    public SStatisticsContainer getContainer(UUID uuid) {
        SStatisticsContainer container = statisticsContainerMap.get(uuid);
        if (container == null) {
            container = new SStatisticsContainer(uuid);
            statisticsContainerMap.put(uuid, container);
            save(container, true);
        }

        return container;
    }

    public Optional<SStatisticsContainer> getIfFound(UUID uuid) {
        return Optional.ofNullable(statisticsContainerMap.get(uuid));
    }

    @Override
    protected void onAdd(SStatisticsContainer sStatisticsContainer) {
        statisticsContainerMap.put(sStatisticsContainer.getUuid(), sStatisticsContainer);
    }

    @Override
    protected void onRemove(SStatisticsContainer sStatisticsContainer) {
        statisticsContainerMap.remove(sStatisticsContainer.getUuid());
    }

    @Override
    public Stream<SStatisticsContainer> stream() {
        return statisticsContainerMap.values().stream();
    }

    @Override
    public Iterator<SStatisticsContainer> iterator() {
        return statisticsContainerMap.values().iterator();
    }
}
