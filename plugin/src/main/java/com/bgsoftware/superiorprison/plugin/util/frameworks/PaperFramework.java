package com.bgsoftware.superiorprison.plugin.util.frameworks;

import com.oop.orangeengine.main.task.StaticTask;
import io.papermc.lib.PaperLib;
import java.util.function.Consumer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PaperFramework implements Framework {
  @Override
  public void loadChunk(World world, int x, int z, Consumer<Chunk> callback) {
    StaticTask.getInstance()
        .ensureSync(
            () ->
                PaperLib.getChunkAtAsync(world, x, z)
                    .whenComplete((chunk, t) -> callback.accept(chunk)));
  }

  @Override
  public void teleport(Player player, Location location) {
    StaticTask.getInstance().ensureSync(() -> PaperLib.teleportAsync(player, location));
  }
}
