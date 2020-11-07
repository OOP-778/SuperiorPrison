package com.bgsoftware.superiorprison.plugin.test.generator.manual;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ObjectSupplier;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.GeneratorTemplate;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementMigrator;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ManualObjectGenerator implements ObjectSupplier {

    @Getter
    private int maxIndex;

    public ManualObjectGenerator(Config config) {
        GlobalVariableMap variableMap = new GlobalVariableMap();
        variableMap.newOrReplace("prisoner", VariableHelper.createNullVariable(SPrisoner.class));
        variableMap.newOrReplace("index", VariableHelper.createVariable(1));
        handleVariableMapCreation(variableMap);

        AtomicReference<String> defaultPrefix = new AtomicReference<>(null);
        config.ifValuePresent("default prefix", String.class, p -> {
            defaultPrefix.set(defaultPrefixReplacer(p));
            config.get("default prefix").get().setObject(defaultPrefix.get());
        });

        // Load the prestiges
        for (ConfigSection ladderObjectSection : config.getSections().values()) {
            try {
                // Try to migrate requirements
                RequirementMigrator.migrate(ladderObjectSection);

                // Migrate old placeholders
                migratePlaceholders(ladderObjectSection);

                // Initialize prestige
                GlobalVariableMap prestigeMap = variableMap.clone();

                // Make sure prestige key is an number
                int index = ladderObjectSection.getAs("index", int.class);
                maxIndex = index;

                // Replace the old index of the prestige to current
                prestigeMap.newOrReplace("index", VariableHelper.createVariable(index));
                handleVariableMapClone(prestigeMap, ladderObjectSection);

                // Get the template
                GeneratorTemplate generatorTemplate = new GeneratorTemplate(ladderObjectSection, prestigeMap);
                if (generatorTemplate.getPrefix() == null && defaultPrefix.get() != null)
                    generatorTemplate.setPrefix(defaultPrefix.get());

                generatorTemplate.initialize(prestigeMap);

                Function<SPrisoner, ParsedObject> parser = prisoner -> {
                    GlobalVariableMap prisonerMap = prestigeMap.clone();
                    prisonerMap.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
                    return ParsedObject.of(
                            ladderObjectSection.getKey(),
                            generatorTemplate,
                            prisonerMap,
                            () -> this.getParser(index + 1).map(f -> f.apply(prisoner)).orElse(null),
                            () -> this.getParser(index - 1).map(f -> f.apply(prisoner)).orElse(null),
                            index
                    );
                };

                registerObject(ladderObjectSection, index, parser);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to load ladder at " + ladderObjectSection.getPath(), throwable);
            }
        }

        // Save the config
        config.save();
    }

    private void migratePlaceholders(ConfigSection prestigeSection) {
        prestigeSection
                .get("commands")
                .ifPresent(cv -> {
                    List<String> asList = cv.getAsList(String.class);
                    cv.setObject(
                            asList
                                    .stream()
                                    .map(v -> v.replace("{prisoner}", "%prisoner#player#name%"))
                                    .collect(Collectors.toList())
                    );
                });

        prestigeSection.ifValuePresent("order", int.class, v -> {
            prestigeSection.set("index", v);
            prestigeSection.set("order", null);
        });
    }

    // Register new ParsedObject function
    protected abstract void registerObject(ConfigSection section, int index, Function<SPrisoner, ParsedObject> parser);

    // Handle new global var map creation
    protected abstract void handleVariableMapCreation(GlobalVariableMap map);

    // Handle global var clone
    protected abstract void handleVariableMapClone(GlobalVariableMap map, ConfigSection section);


    public String defaultPrefixReplacer(String in) {
        return in;
    }
}
