package com.bgsoftware.superiorprison.plugin.hook;

import java.util.function.Supplier;

public interface HookClassSupplier<T extends SHook> extends Supplier<T> {

    Class<SHook> getWithIO() throws Throwable;

}
