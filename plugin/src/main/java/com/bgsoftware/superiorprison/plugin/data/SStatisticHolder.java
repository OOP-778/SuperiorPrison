package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.StatisticsController;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.google.common.collect.Maps;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.universal.UniversalStorage;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class SStatisticHolder extends UniversalStorage<SStatisticsContainer>
    implements StatisticsController {
  private final Map<UUID, SStatisticsContainer> containerMap = Maps.newConcurrentMap();

  public SStatisticHolder(DatabaseController controller) {
    super(controller);
    addVariant("statistics", SStatisticsContainer.class);

    currentImplementation(
        (Storage<SStatisticsContainer>)
            SuperiorPrisonPlugin.getInstance()
                .getMainConfig()
                .getStorageSection()
                .getStorageProvider()
                .apply(this));
  }

  @Override
  public SStatisticsContainer getContainer(UUID uuid) {
    SStatisticsContainer container = containerMap.get(uuid);
    if (container == null) {
      container = new SStatisticsContainer(uuid);
      containerMap.put(uuid, container);

      save(container, true);
    }

    return container;
  }

  public Optional<SStatisticsContainer> getIfFound(UUID uuid) {
    return Optional.ofNullable(containerMap.get(uuid));
  }

  @Override
  protected void onAdd(SStatisticsContainer sStatisticsContainer) {
    containerMap.put(sStatisticsContainer.getUuid(), sStatisticsContainer);
  }

  @Override
  protected void onRemove(SStatisticsContainer sStatisticsContainer) {
    containerMap.remove(sStatisticsContainer.getUuid());
  }

  @Override
  public Stream<SStatisticsContainer> stream() {
    return containerMap.values().stream();
  }

  @Override
  public Iterator<SStatisticsContainer> iterator() {
    return containerMap.values().iterator();
  }
}
