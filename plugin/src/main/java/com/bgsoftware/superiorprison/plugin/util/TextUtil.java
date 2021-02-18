package com.bgsoftware.superiorprison.plugin.util;

import static com.oop.orangeengine.main.Helper.capitalizeAll;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.google.gson.internal.Primitives;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

public class TextUtil {

  public static <T> List<String> replaceList(
      T object,
      Collection<String> multiLine,
      Set<OPair<String, Function<T, String>>> placeholders) {
    return multiLine.stream()
        .map(line -> replaceText(object, line, placeholders))
        .collect(Collectors.toList());
  }

  public static <T> String apply(
      T object, String text, Set<OPair<String, Function<T, String>>> placeholders) {
    String[] array = new String[] {text};
    placeholders.forEach(
        placeholder ->
            array[0] =
                array[0].replace(
                    placeholder.getFirst(), String.valueOf(placeholder.getSecond().apply(object))));
    return array[0];
  }

  public static <T> String replaceText(
      T object, String text, Set<OPair<String, Function<T, String>>> placeholders) {
    String[] array = new String[] {text};
    array[0] = apply(object, array[0], placeholders);
    SuperiorPrisonPlugin.getInstance()
        .getHookController()
        .executeIfFound(() -> PapiHook.class, papi -> array[0] = papi.parse(object, array[0]));
    return array[0];
  }

  public static String mergeText(String[] text) {
    boolean first = true;
    StringBuilder stringBuilder = new StringBuilder();
    for (String s : text) {
      if (first) {
        stringBuilder.append(s);
        first = false;

      } else stringBuilder.append(" ").append(s);
    }
    return stringBuilder.toString();
  }

  public static String beautify(Object object) {
    if (object == null) return "null";

    if (Primitives.wrap(object.getClass()).isAssignableFrom(Number.class))
      return beautifyNumber(Double.parseDouble(object.toString()));
    else if (Primitives.wrap(object.getClass()) == Boolean.class)
      return (Boolean) object ? "true" : "false";
    else return beautify(object.toString());
  }

  public static String beautify(String text) {
    return capitalizeAll(text.toLowerCase().replace("_", " "));
  }

  public static String beautifyName(@NonNull ItemStack item) {
    return beautify(
        (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            ? item.getItemMeta().getDisplayName()
            : OMaterial.matchMaterial(item).name());
  }

  public static String beautifyNumber(Number d) {
    return beautifyNumber(d.toString());
  }

  public static String beautifyNumber(String stringD) {
    if (stringD.contains(".")) {
      String[] split = stringD.split("\\.");
      for (char c : split[1].toCharArray())
        if (c != '0')
          return NumberFormat.getNumberInstance(Locale.US).format(Double.valueOf(stringD));

      return NumberFormat.getNumberInstance(Locale.US).format(Long.valueOf(split[0]));
    }
    return NumberFormat.getNumberInstance(Locale.US).format(Long.valueOf(stringD));
  }
}
