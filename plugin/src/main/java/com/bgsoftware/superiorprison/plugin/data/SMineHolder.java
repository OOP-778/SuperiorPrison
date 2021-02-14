package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.MineHolder;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.reset.MineResetQueue;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.google.common.collect.Maps;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.universal.UniversalStorage;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.material.OMaterial;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.bukkit.Location;

public class SMineHolder extends UniversalStorage<SNormalMine> implements MineHolder {

  private final Map<String, SNormalMine> mineMap = Maps.newConcurrentMap();

  @Getter private final MineResetQueue queue = new MineResetQueue();

  private final OCache<UUID, List<SNormalMine>> minesCache =
      OCache.builder()
          .concurrencyLevel(1)
          .resetExpireAfterAccess(true)
          .expireAfter(5, TimeUnit.SECONDS)
          .build();

  public SMineHolder(DatabaseController controller) {
    super(controller);

    addVariant("mines", SNormalMine.class);
    currentImplementation(
        (Storage<SNormalMine>)
            SuperiorPrisonPlugin.getInstance()
                .getMainConfig()
                .getStorageSection()
                .getStorageProvider()
                .apply(this));
  }

  @Override
  public Set<SuperiorMine> getMines() {
    return getMines(null);
  }

  public Set<SuperiorMine> getMines(Predicate<SuperiorMine> filter) {
    return stream()
        .map(mine -> (SuperiorMine) mine)
        .filter(mine -> filter == null || filter.test(mine))
        .collect(Collectors.toSet());
  }

  public Set<String> getMinesWorlds() {
    return stream().map(mine -> mine.getWorld().getName()).collect(Collectors.toSet());
  }

  @Override
  public Optional<SuperiorMine> getMine(String mineName) {
    return Optional.ofNullable(mineMap.get(mineName));
  }

  @Override
  public Optional<SuperiorMine> getMineAt(Location location) {
    return stream()
        .filter(mine -> mine.isInside(location))
        .map(mine -> (SuperiorMine) mine)
        .findFirst();
  }

  public List<SNormalMine> getMinesFor(SPrisoner prisoner) {
    List<SNormalMine> mines = minesCache.get(prisoner.getUUID());
    if (mines != null) return mines;

    mines =
        stream()
            .filter(
                mine ->
                    prisoner.getPlayer().hasPermission("prison.admin.editmine")
                        || (mine.canEnter(prisoner) && mine.getSettings().isTeleportation()))
            .sorted(Comparator.comparing(SNormalMine::getName))
            .collect(Collectors.toList());
    minesCache.put(prisoner.getUUID(), mines);
    return mines;
  }

  public void clear() {
    minesCache.clear();
    for (SNormalMine value : mineMap.values()) value.clean();

    mineMap.clear();
  }

  @Override
  protected void onAdd(SNormalMine sNormalMine) {
    mineMap.put(sNormalMine.getName(), sNormalMine);
  }

  @Override
  protected void onRemove(SNormalMine sNormalMine) {
    mineMap.remove(sNormalMine.getName());
  }

  @Override
  public Stream<SNormalMine> stream() {
    return mineMap.values().stream();
  }

  @Override
  public Iterator<SNormalMine> iterator() {
    return mineMap.values().iterator();
  }
}
