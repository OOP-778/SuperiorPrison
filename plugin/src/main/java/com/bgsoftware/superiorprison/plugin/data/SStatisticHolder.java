package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.StatisticController;
import com.bgsoftware.superiorprison.api.data.statistic.StatisticContainer;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticContainer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.DatabaseController;
import com.oop.orangeengine.database.DatabaseHolder;
import com.oop.orangeengine.database.DatabaseObject;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class SStatisticHolder implements DatabaseHolder<UUID, SStatisticContainer>, StatisticController {

    private Map<UUID, SStatisticContainer> containers = Maps.newConcurrentMap();

    private DatabaseController databaseController;

    public SStatisticHolder(DatabaseController databaseController) {
        this.databaseController = databaseController;
    }

    @Override
    public Stream<SStatisticContainer> dataStream() {
        return containers.values().stream();
    }

    @Override
    public UUID generatePrimaryKey(SStatisticContainer container) {
        return container.getUuid();
    }

    @Override
    public Set<Class<? extends DatabaseObject>> getObjectVariants() {
        return Sets.newHashSet(SStatisticContainer.class);
    }

    @Override
    public DatabaseController getDatabaseController() {
        return databaseController;
    }

    @Override
    public void onAdd(SStatisticContainer sStatisticContainer, boolean b) {
        containers.put(sStatisticContainer.getUuid(), sStatisticContainer);
    }

    @Override
    public void onRemove(SStatisticContainer sStatisticContainer) {
        containers.remove(sStatisticContainer);
    }

    @Override
    public Optional<StatisticContainer> getContainer(UUID uuid) {
        return Optional.ofNullable(containers.get(uuid));
    }
}
