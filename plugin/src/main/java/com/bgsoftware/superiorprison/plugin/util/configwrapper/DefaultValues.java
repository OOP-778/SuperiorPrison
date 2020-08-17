package com.bgsoftware.superiorprison.plugin.util.configwrapper;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.ConfigValue;
import com.oop.orangeengine.yaml.interfaces.Valuable;

import java.util.Arrays;
import java.util.Map;

public interface DefaultValues {
    Map<String, OPair<Object, String[]>> getDefaultValues();

    default void addDefault(String path, Object value, String... comments) {
        getDefaultValues().put(path, new OPair<>(value, comments));
    }

    default void _init(Valuable valuable) {
        getDefaultValues().forEach((path, value) -> {
            if (!valuable.isValuePresent(path)) {
                ConfigValue set = valuable.set(path, value.getFirst());
                set.comments.addAll(Arrays.asList(value.getSecond()));
            }
        });
    }
}
