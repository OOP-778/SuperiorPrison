package com.bgsoftware.superiorprison.plugin.test.generator.auto;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.generator.ObjectSupplier;
import com.bgsoftware.superiorprison.plugin.test.generator.ObjectTemplate;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.interfaces.Valuable;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class ObjectGenerator<G extends GeneratorOptions> implements ObjectSupplier {
    private ObjectTemplate template;

    @Getter
    private final GlobalVariableMap variableMap = new GlobalVariableMap();

    @Getter
    private final G options;

    private OCache<UUID, Map<Integer, ParsedObject>> parsedCache =
            OCache
                    .builder()
                    .concurrencyLevel(1)
                    .expireAfter(3, TimeUnit.SECONDS)
                    .build();

    private Map<Integer, ObjectTemplate> specificCache = new ConcurrentHashMap<>();

    public ObjectGenerator(Valuable valuable) {
        if (!valuable.isSectionPresent("template") || !valuable.isSectionPresent("options"))
            throw new IllegalStateException("Invalid Object Generator. Missing sections...");

        variableMap.newVariable("prisoner", VariableHelper.createNullVariable(SPrisoner.class));
        initializeMap();

        template = new ObjectTemplate(valuable.getSection("template").get(), variableMap);

        // Load options
        ConfigSection options = valuable.getSection("options").get();
        this.options = GeneratorOptions.of(options, variableMap);

        options.ifSectionPresent("specific", specificsSection -> {
            for (ConfigSection specificSection : specificsSection.getSections().values()) {
                ObjectTemplate clone = template.clone();
                specificSection.ifValuePresent("commands", List.class, cmds -> clone.getCommands().addAll(cmds));

                // Check for message
                if (specificSection.isSectionPresent("message")) {
                    clone.setMessage(YamlMessage.load(specificSection.getSection("message").get()));

                } else if (specificSection.isValuePresent("message"))
                    clone.setMessage(new OChatMessage(specificSection.getAs("message", String.class)));

                specificSection.ifSectionPresent("requirements", reqSection -> {
                    clone.getRequirements().getHoldingData().addAll(Testing.controller.initializeRequirementsSection(reqSection, variableMap).getHoldingData());
                });

                specificCache.put(this.options.getIndex(specificSection.getKey()), clone);
            }
        });

        template.initialize(variableMap);
        for (ObjectTemplate value : specificCache.values())
            value.initialize(variableMap);
    }

    public boolean isValid(Object object) {
        return options.isValid(options.getIndex(object));
    }

    public boolean hasNext(Object object) {
        return options.hasNext(options.getIndex(object));
    }

    public ParsedObject getParsed(SPrisoner prisoner, Object ob) {
        int level = options.getIndex(ob);

        Map<Integer, ParsedObject> cache = parsedCache.get(prisoner.getUUID());
        ParsedObject object;
        if (cache != null) {
            object = cache.get(level);
            if (object != null)
                return object;
        }

        if (cache == null) {
            cache = new HashMap<>();
            parsedCache.put(prisoner.getUUID(), cache);
        }

        ParsedObject parsed = parse(prisoner, level);
        cache.put(level, parsed);
        return parsed;
    }

    public ObjectTemplate getTemplate(int prestige) {
        ObjectTemplate template = specificCache.get(prestige);
        if (template != null) return template;

        return this.template;
    }

    protected abstract ParsedObject parse(SPrisoner prisoner, int level);

    protected abstract void initializeMap();
}
