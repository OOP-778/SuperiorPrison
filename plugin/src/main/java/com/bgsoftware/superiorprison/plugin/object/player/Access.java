package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import org.bukkit.Bukkit;

import java.util.List;

import static com.bgsoftware.superiorprison.plugin.util.HookUtil.findHook;

public interface Access {
    List<String> getPermissions();

    List<String> getCommands();

    default void onAdd(SPrisoner prisoner) {
        getCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{prisoner}", prisoner.getOfflinePlayer().getName())));
        findHook(() -> VaultHook.class).ifPresent(hook -> hook.addPermissions(prisoner, getPermissions()));
    }

    default void onRemove(SPrisoner prisoner) {
        findHook(() -> VaultHook.class).ifPresent(hook -> hook.removePermissions(prisoner, getPermissions()));
    }
}
