package com.bgsoftware.superiorprison.plugin.hook;

public interface HookClassSupplier<T extends SHook> {
  Class<T> getWithIO() throws Throwable;
}
