package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.HookClassSupplier;
import com.bgsoftware.superiorprison.plugin.hook.SHook;

import java.util.Optional;
import java.util.function.Consumer;

public class HookUtil {
    public static <T extends SHook> Optional<T> findHook(HookClassSupplier<T> supplier) {
        return SuperiorPrisonPlugin.getInstance().getHookController().findHook(supplier);
    }

    public static <T extends SHook> void executeIfFound(HookClassSupplier<T> supplier, Consumer<T> consumer) {
        findHook(supplier).ifPresent(consumer);
    }
}
