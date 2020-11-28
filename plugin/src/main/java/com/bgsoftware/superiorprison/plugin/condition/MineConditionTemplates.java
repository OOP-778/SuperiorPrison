package com.bgsoftware.superiorprison.plugin.condition;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MineConditionTemplates {
    private static final Map<String, MineConditionTemplate> templateMap = new ConcurrentHashMap<>();
    public static Map<String, MineConditionTemplate> getTemplateMap() {
        return templateMap;
    }

    public static void registerTemplate(Consumer<MineConditionTemplate> templateConsumer) {
        MineConditionTemplate template = new MineConditionTemplate();
        templateConsumer.accept(template);

        templateMap.put(template.name(), template);
    }

    static {
        registerTemplate(template -> {
            template.name("Permission");
            template.description("With this template you can create", "Condition based of permission");
            template.parser(parser -> {
                parser.addField(
                        "permission",
                        "Please define what permission you would like to use. You can use variables in it.",
                        input -> input
                );

                parser.conditionProvider(values -> {
                    String permission = values.get("permission").toString();
                    return "%prisoner% has permission '" + permission + "'";
                });
            });
        });

        registerTemplate(template -> {
            template.name("Rank");
            template.description("With this template you can create", "Conditions based of ranks");
            template.parser(parser -> {
                parser.addField(
                        "rank",
                        "Please define what rank should be the minimum to access this mine",
                        input -> {
                            BigInteger index = SuperiorPrisonPlugin.getInstance().getRankController().getIndex(input);
                            if (NumberUtil.equals(index, BigInteger.valueOf(-1)))
                                throw new IllegalStateException("Invalid rank by " + input);

                            return input;
                        }
                );

                parser.conditionProvider(values -> {
                    String rank = values.get("rank").toString();
                    BigInteger index = SuperiorPrisonPlugin.getInstance().getRankController().getIndex(rank);

                    return "%prisoner#ladderrank% >= " + index.toString();
                });
            });
        });

        registerTemplate(template -> {
            template.name("Prestige");
            template.description("With this template you can create", "Conditions based of prestiges");
            template.parser(parser -> {
                parser.addField(
                        "prestige",
                        "Please define what prestige should be the minimum to access this mine",
                        input -> {
                            BigInteger index = SuperiorPrisonPlugin.getInstance().getPrestigeController().getIndex(input);
                            if (NumberUtil.equals(index, BigInteger.valueOf(-1)))
                                throw new IllegalStateException("Invalid prestige by " + input);

                            return input;
                        }
                );

                parser.conditionProvider(values -> {
                    String rank = values.get("prestige").toString();
                    BigInteger index = SuperiorPrisonPlugin.getInstance().getRankController().getIndex(rank);

                    return "%prisoner#ladderrank% >= " + index.toString();
                });
            });
        });
    }

}
