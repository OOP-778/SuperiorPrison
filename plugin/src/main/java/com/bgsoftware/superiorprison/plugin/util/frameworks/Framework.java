package com.bgsoftware.superiorprison.plugin.util.frameworks;

import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.version.OVersion;
import io.papermc.lib.PaperLib;
import java.util.function.Consumer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface Framework {
  Framework FRAMEWORK = requestFramework();

  static Framework requestFramework() {
    return PaperLib.isPaper() && OVersion.isOrAfter(13)
        ? new PaperFramework()
        : new SpigotFramework();
  }

  default void loadChunk(World world, int x, int z, Consumer<Chunk> callback) {
    StaticTask.getInstance()
        .ensureSync(
            () -> {
              Chunk chunkAt = world.getChunkAt(x, z);
              callback.accept(chunkAt);
            });
  }

  default void teleport(Player player, Location location) {
    StaticTask.getInstance().ensureSync(() -> player.teleport(location));
  }
}
