package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.HookClassSupplier;
import com.bgsoftware.superiorprison.plugin.hook.SHook;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class HookController {
    private final Map<Class<?>, SHook> hooks = new HashMap<>();

    public HookController() {
    }

    public void registerHooks(HookClassSupplier... hooks) {
        for (HookClassSupplier<?> clazzSupplier : hooks) {
            Class<? extends SHook> clazz = getClazz(clazzSupplier);
            if (clazz == null) continue;

            try {
                SHook hook = clazz.newInstance();
                if (!hook.isLoaded()) {
                    if (!hook.isRequired()) continue;
                    else
                        throw new IllegalStateException("Failed to hook into " + hook.getPluginName() + " because it's not loaded and it is required!");
                }

                this.hooks.put(clazz, hook);
                SuperiorPrisonPlugin.getInstance().getOLogger().print("Hooked into (" + hook.getPlugin().getName() + ") " + hook.getPlugin().getDescription().getVersion());
            } catch (Throwable throwable) {
                if (throwable instanceof IllegalStateException || throwable instanceof NullPointerException)
                    throw new IllegalStateException("Failed to start HookController", throwable);
            }
        }
    }

    public <T extends SHook> void executeIfFound(HookClassSupplier<T> supplier, Consumer<T> consumer) {
        findHook(supplier).ifPresent(consumer);
    }

    public <T extends SHook> void executeIfFound(HookClassSupplier<T> supplier, Runnable runnable) {
        executeIfFound(supplier, (hook) -> runnable.run());
    }

    public <T extends SHook> Optional<T> findHook(HookClassSupplier<T> supplier) {
        Class<T> clazz = getClazz(supplier);
        if (clazz == null) return Optional.empty();

        return (Optional<T>) Optional.ofNullable(hooks.get(clazz));
    }

    public void disableIf(SHook sHook, boolean b, String s) {
        hooks.remove(sHook.getClass());
        if (b) {
            SuperiorPrisonPlugin.getInstance().getOLogger().printWarning(sHook.getPluginName() + " failed to hook, cause: " + s);
        }
    }

    public <T extends SHook> Class<T> getClazz(HookClassSupplier<T> supplier) {
        try {
            return supplier.getWithIO();
        } catch (Throwable ex) {
            return null;
        }
    }
}
