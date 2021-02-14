package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.bomb.BombConfig;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.nbt.NBTItem;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BombController implements OComponent<SuperiorPrisonPlugin> {
  private final Map<String, BombConfig> bombs = new HashMap<>();
  private final Map<UUID, Map<String, Long>> cooldown = new ConcurrentHashMap<>();

  public boolean isBomb(ItemStack itemStack) {
    if (itemStack == null || itemStack.getType() == Material.AIR) return false;
    return new NBTItem(itemStack).hasKey("SP_BOMB");
  }

  public Optional<BombConfig> getBombOf(String bombName) {
    return Optional.ofNullable(bombs.get(bombName.toLowerCase()));
  }

  public Optional<BombConfig> getBombOf(ItemStack itemStack) {
    if (itemStack == null || itemStack.getType() == Material.AIR) return Optional.empty();

    NBTItem nbtItem = new NBTItem(itemStack);
    if (!nbtItem.hasKey("SP_BOMB")) return Optional.empty();

    return Optional.ofNullable(bombs.get(nbtItem.getString("SP_BOMB").toLowerCase()));
  }

  public long getCooldown(Player player, BombConfig bombConfig) {
    return Optional.ofNullable(cooldown.get(player.getUniqueId()))
        .map(map -> map.get(bombConfig.getName().toLowerCase()))
        .orElse(-1L);
  }

  public void removeCooldown(Player player, BombConfig config) {
    Optional.ofNullable(cooldown.get(player.getUniqueId()))
        .ifPresent(map -> map.remove(config.getName().toLowerCase()));
  }

  public void putCooldown(Player player, BombConfig config) {
    if (config.getCooldown() == 0) return;
    cooldown
        .computeIfAbsent(player.getUniqueId(), key -> new ConcurrentHashMap<>())
        .put(
            config.getName().toLowerCase(),
            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(config.getCooldown()));
  }

  @Override
  public boolean load() {
    bombs.clear();
    cooldown.clear();
    Config bombsConfig = getPlugin().getConfigController().getBombsConfig();

    for (ConfigSection value : bombsConfig.getSections().values())
      bombs.put(value.getKey().toLowerCase(), new BombConfig(value));

    return true;
  }

  public Collection<String> getBombs() {
    return bombs.keySet();
  }
}
