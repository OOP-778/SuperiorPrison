package com.bgsoftware.superiorprison.plugin.object.player;

import static com.bgsoftware.superiorprison.plugin.util.HookUtil.findHook;

import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import java.util.List;
import org.bukkit.Bukkit;

public interface Access {
  List<String> getPermissions();

  List<String> getCommands();

  default void onAdd(SPrisoner prisoner) {
    getCommands()
        .forEach(
            command ->
                Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    command.replace("{prisoner}", prisoner.getOfflinePlayer().getName())));
    findHook(() -> VaultHook.class)
        .ifPresent(hook -> hook.addPermissions(prisoner, getPermissions()));
  }

  default void onRemove(SPrisoner prisoner) {
    findHook(() -> VaultHook.class)
        .ifPresent(hook -> hook.removePermissions(prisoner, getPermissions()));
  }
}
