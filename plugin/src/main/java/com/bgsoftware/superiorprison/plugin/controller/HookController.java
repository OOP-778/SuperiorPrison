package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.SHook;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class HookController {

    private Map<Class<?>, SHook> hooks = new HashMap<>();
    public HookController() {

    }

    public void registerHooks(Class<? extends SHook> ...hooks) {
        for (Class<? extends SHook> hookClazz : hooks) {
            try {
                SHook hook = hookClazz.newInstance();
                if (!hook.isLoaded()) continue;

                this.hooks.put(hookClazz, hook);
                SuperiorPrisonPlugin.getInstance().getOLogger().print("Hooked into (" + hook.getPlugin().getName() + ") " + hook.getPlugin().getDescription().getVersion());
            } catch (Throwable ignored) {}
        }
    }

    public <T extends SHook> void executeIfFound(Class<T> hookClazz, Consumer<T> consumer) {
        findHook(hookClazz).ifPresent(consumer);
    }

    public <T extends SHook> void executeIfFound(Class<T> hookClazz, Runnable runnable) {
        executeIfFound(hookClazz, (hook) -> runnable.run());
    }

    public <T extends SHook> Optional<T> findHook(Class<T> hookClazz) {
        return (Optional<T>) Optional.ofNullable(hooks.get(hookClazz));
    }

    public void disableIf(SHook sHook, boolean b, String s) {
        hooks.remove(sHook.getClass());
        if (b) {
            SuperiorPrisonPlugin.getInstance().getOLogger().printWarning(sHook.getPluginName() + " failed to hook, cause: " + s);
        }
    }
}
