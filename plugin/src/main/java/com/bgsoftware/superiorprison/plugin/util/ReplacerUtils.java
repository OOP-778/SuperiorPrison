package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ReplacerUtils {

    public static <T> List<String> replaceList(T object, Collection<String> multiLine, Set<BiFunction<String, T, String>> placeholders, Optional<PapiHook> papi) {
        return multiLine
                .stream()
                .map(line -> {
                    String[] array = new String[]{line};
                    placeholders.forEach(f -> array[0] = f.apply(array[0], object));
                    papi.ifPresent(papiHook -> array[0] = papiHook.parse(object, array[0]));
                    return array[0];
                })
                .collect(Collectors.toList());
    }

    public static <T> String replaceText(T object, String text, Set<BiFunction<String, T, String>> placeholders, Optional<PapiHook> papi) {
        String[] array = new String[]{text};
        placeholders.forEach(f -> array[0] = f.apply(array[0], object));
        papi.ifPresent(papiHook -> array[0] = papiHook.parse(object, array[0]));

        return array[0];
    }

}
