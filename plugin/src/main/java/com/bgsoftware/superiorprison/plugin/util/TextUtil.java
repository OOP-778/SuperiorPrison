package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.oop.orangeengine.main.Helper.capitalizeAll;

public class TextUtil {

    public static <T> List<String> replaceList(T object, Collection<String> multiLine, Set<OPair<String, Function<T, String>>> placeholders) {
        return multiLine
                .stream()
                .map(line -> replaceText(object, line, placeholders))
                .collect(Collectors.toList());
    }

    public static <T> String apply(T object, String text, Set<OPair<String, Function<T, String>>> placeholders) {
        String[] array = new String[]{text};
        placeholders.forEach(placeholder -> array[0] = array[0].replace(placeholder.getFirst(), String.valueOf(placeholder.getSecond().apply(object))));
        return array[0];
    }

    public static <T> String replaceText(T object, String text, Set<OPair<String, Function<T, String>>> placeholders) {
        String[] array = new String[]{text};
        array[0] = apply(object, array[0], placeholders);
        SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> PapiHook.class, papi -> array[0] = papi.parse(object, array[0]));
        return array[0];
    }

    public static String mergeText(String[] text) {
        boolean first = true;
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : text) {
            if (first) {
                stringBuilder.append(s);
                first = false;

            } else
                stringBuilder.append(" ").append(s);
        }
        return stringBuilder.toString();
    }

    public static String beautify(Object object) {
        if (object instanceof Double || object.getClass() == double.class)
            return beautifyDouble(Double.parseDouble(object.toString()));

        else
            return beautify(object.toString());
    }

    public static String beautify(String text) {
        return capitalizeAll(text.toLowerCase().replace("_", " "));
    }

    public static String beautifyName(@NonNull ItemStack item) {
        return beautify((item.hasItemMeta() && item.getItemMeta().hasDisplayName()) ? item.getItemMeta().getDisplayName() : OMaterial.matchMaterial(item).name());
    }

    public static String beautifyDouble(Double d) {
        String stringD = String.valueOf(d);
        if (stringD.contains(".")) {
            String split[] = stringD.split("\\.");
            if (split[1].contentEquals("0") && split[1].length() == 1)
                return split[0];

            else
                return stringD;
        }
        return stringD;
    }
}
