package com.bgsoftware.superiorprison.plugin.util;

public class BenchmarkUtil {
    public static void benchmark(String name, Runnable runnable) {
        ClassDebugger.debug("==== " + "Starting benchmark: " + name + " ====");
        long start = System.currentTimeMillis();
        runnable.run();
        ClassDebugger.debug("==== " + "Done benchmark: " + name + " took (" + (System.currentTimeMillis() - start) + ") ====");
    }
}
