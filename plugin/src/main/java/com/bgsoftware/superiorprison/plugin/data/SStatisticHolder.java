package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.StatisticsController;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.UUID;

public class SStatisticHolder extends UniversalDataHolder<UUID, SStatisticsContainer> implements StatisticsController {

    public SStatisticHolder(DatabaseController controller) {
        super(controller, SStatisticsContainer::getUuid);

        String type = SuperiorPrisonPlugin.getInstance().getMainConfig().getDatabase().getType();
        if (type.equalsIgnoreCase("flat")) {
            currentHolder(
                    DataSettings.builder(DataSettings.FlatStorageSettings.class, SStatisticsContainer.class)
                            .directory(new File(SuperiorPrisonPlugin.getInstance().getDataFolder() + "/statistics"))
                            .variants(ImmutableMap.of("statisticsContainer", SStatisticsContainer.class))
            );
        } else if (type.equalsIgnoreCase("sqlite") || type.equalsIgnoreCase("mysql")) {
            currentHolder(
                    DataSettings.builder(DataSettings.SQlSettings.class, SStatisticsContainer.class)
                            .databaseWrapper(controller.getDatabase())
                            .variants(new Class[]{SStatisticsContainer.class})
            );
        }
    }

    @Override
    public SStatisticsContainer getContainer(UUID uuid) {
        SStatisticsContainer container = getDataMap().get(uuid);
        if (container == null) {
            container = new SStatisticsContainer(uuid);
            getDataMap().put(uuid, container);
            save(container, true);
        }

        return container;
    }
}
