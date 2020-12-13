package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.function.Supplier;

public class Analyzer {
    public static <T> OPair<T, Long> analyzeTook(Supplier<T> supplier) {
        long start = System.currentTimeMillis();
        return new OPair<>(supplier.get(), (System.currentTimeMillis() - start));
    }
}
