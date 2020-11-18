package com.bgsoftware.superiorprison.plugin.requirement;

import com.google.common.base.Preconditions;
import com.oop.orangeengine.yaml.ConfigSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class RequirementMigrator {
    private static final Map<String, BiConsumer<Map<String, String>, ConfigSection>> migrators = new HashMap<>();

    static {
        // Register ECO migration
        migrators.put("eco", (data, section) -> {
            ConfigSection ecoSection = section.createSection("eco");

            ecoSection.getComments().add("Automatically Migrated");
            ecoSection.getComments().add("If there's any issues, please contact my developer!");

            // Set the display name of the requirement
            ecoSection.set("display", "Money");

            // Set the value of the requirement
            ecoSection.set("value", data.get("value"));

            // Set the getter of current value for the requirement
            ecoSection.set("getter", "get balance of %prisoner%");

            // Set the checker of the requirement
            ecoSection.set("checker", "%getter% >= %value%");

            // Set the taker if present of the requirement
            if (data.get("take") == null || Boolean.parseBoolean(data.get("take")))
                ecoSection.set("taker", "take %value% from %prisoner% balance");
        });

        // Register XP_LEVEL migration
        migrators.put("xp_level", (data, section) -> {
            ConfigSection xp_level = section.createSection("xp_level");
            xp_level.getComments().add("Automatically Migrated");
            xp_level.getComments().add("If there's any issues, please contact my developer!");

            xp_level.set("display", "Xp Level");
            xp_level.set("value", data.get("value"));
            xp_level.set("getter", "get xp level of %prisoner%");
            xp_level.set("checker", "%getter% >= %value%");

            if (data.get("take") == null || Boolean.parseBoolean(data.get("take")))
                xp_level.set("taker", "set %prisoner% xp level to {%getter% - %value%}");
        });

        // Add PAPI migrator
        migrators.put("papi", (data, section) -> {
            String placeholder = data.get("placeholder");
            ConfigSection papi = section.createSection("papi");
            papi.getComments().add("Automatically Migrated");
            papi.getComments().add("If there's any issues, please contact my developer!");

            papi.set("display", "Placeholder");
            papi.set("value", data.get("value"));
            papi.set("getter", "parse '" + placeholder + "' placeholder as %prisoner%");
            papi.set("checker", "%getter% >= %value%");
        });

        // Add xp migrator
        migrators.put("xp", (data, section) -> {
            ConfigSection xp = section.createSection("xp");
            xp.getComments().add("Automatically Migrated");
            xp.getComments().add("If there's any issues, please contact my developer!");

            xp.set("display", "Experience");
            xp.set("value", data.get("value"));
            xp.set("getter", "get xp of %prisoner%");
            xp.set("checker", "%getter% >= %value%");

            if (data.get("take") == null || Boolean.parseBoolean(data.get("take")))
                xp.set("taker", "set %prisoner% xp to {%getter% - %value%}");
        });

        // Add rank migrator
        migrators.put("rank", (data, section) -> {

        });
    }

    public static void migrate(ConfigSection section) {
        // Check for requirements value
        if (!section.isValuePresent("requirements")) return;

        List<String> unmigrated = section.getAs("requirements");

        // Remove the requirements value from config
        section.set("requirements", null);

        // Create requirements section
        ConfigSection requirements = section.createSection("requirements");

        for (String s : unmigrated) {
            Map<String, String> allData = new HashMap<>();
            String[] allSplit = s.split("}");

            String value = allSplit[1];
            allData.put("value", value.startsWith(" ") ? value.substring(1) : value);

            String splitPart1 = allSplit[0];
            if (splitPart1.startsWith(" "))
                splitPart1 = splitPart1.substring(1);

            String[] dataSplit = splitPart1.split(",");
            allData.put("type", dataSplit[0].startsWith("{") ? dataSplit[0].substring(1) : dataSplit[0]);

            String key = null;
            for (int index = 1; index < dataSplit.length; index++) {
                String pair = dataSplit[index];
                for (String o : pair.split("=")) {
                    if (key == null) {
                        key = o.startsWith(" ") ? o.substring(1) : o;
                    } else {
                        allData.put(key, o);
                        key = null;
                    }
                }
            }

            BiConsumer<Map<String, String>, ConfigSection> migrator = migrators.get(allData.get("type").toLowerCase());
            Preconditions.checkArgument(migrator != null, "Failed to find migrator for requirement type " + allData.get("type"));

            migrator.accept(allData, requirements);
        }
    }
}
