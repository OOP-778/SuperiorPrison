package com.bgsoftware.superiorprison.plugin.util.input.multi;

import org.bukkit.entity.Player;

public interface MultiInputCompletion {
  void complete(Player player, MultiInputData data);
}
