package com.bgsoftware.superiorprison.plugin.util.script;

import lombok.NonNull;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class RegexCreator {
    public static String VARIABLE_VARIANT = "[0-9]+V";
    public static String NUMBER_VARIANT = "[-]*[0-9]+";

    private final List<String> groups = new LinkedList<>();

    public RegexCreator add(@NonNull Consumer<VariantCreator> consumer) {
        VariantCreator variantCreator = new VariantCreator();
        consumer.accept(variantCreator);
        groups.add(variantCreator.toString());
        return this;
    }

    public RegexCreator addFromClone(int index) {
        groups.add(groups.get(index));
        return this;
    }

    public RegexCreator add(String input) {
        groups.add(input);
        return this;
    }

    public RegexCreator add(String input, boolean optional, boolean matching) {
        return add(group -> {
            group.addVariant(input);
            group.optional = optional;
            group.matching = matching;
        });
    }

    public String buildToString() {
        StringBuilder builder = new StringBuilder();
        String[] strings = groups.toArray(new String[0]);
        for (int i = 0; i < strings.length; i++) {
            builder.append(strings[i]);
            if (i != (strings.length - 1))
                builder.append("(?:\\s)*");
        }

        return builder.toString();
    }

    public Pattern compile() {
        return Pattern.compile(buildToString(), Pattern.CASE_INSENSITIVE);
    }

    // Converts into
    // (?:)
    public static class VariantCreator {
        @Setter
        private boolean optional = false;
        @Setter
        private boolean matching = true;

        private final List<String> possibleVariants = new LinkedList<>();

        public void addVariant(String variant) {
            possibleVariants.add(variant);
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (matching || optional || possibleVariants.size() > 1)
                builder.append("(");

            if (!matching)
                builder.append("?:");

            if (possibleVariants.size() == 1)
                builder.append(possibleVariants.get(0));
            else {
                String[] objects = possibleVariants.toArray(new String[1]);
                for (int i = 0; i < objects.length; i++) {
                    builder.append(objects[i]);
                    if (i != (objects.length - 1))
                        builder.append("|");
                }
            }

            if (matching || optional || possibleVariants.size() > 1)
                builder.append(")");

            if (optional)
                builder.append("*");

            return builder.toString();
        }
    }
}
