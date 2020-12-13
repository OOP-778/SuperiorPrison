package com.bgsoftware.superiorprison.plugin.ladder.generator.auto;

import com.bgsoftware.superiorprison.plugin.ladder.LadderTemplate;
import com.bgsoftware.superiorprison.plugin.ladder.ObjectSupplier;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.requirement.RequirementController;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.interfaces.Valuable;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class ObjectGenerator<G extends GeneratorOptions> implements ObjectSupplier {
    private final LadderTemplate template;

    @Getter
    private final GlobalVariableMap variableMap = new GlobalVariableMap();

    @Getter
    @NonNull
    private final G options;

    private final OCache<UUID, Map<BigInteger, ParsedObject>> parsedCache =
            OCache
                    .builder()
                    .concurrencyLevel(1)
                    .expireAfter(3, TimeUnit.SECONDS)
                    .build();

    private final Map<BigInteger, LadderTemplate> specificCache = new ConcurrentHashMap<>();

    public ObjectGenerator(Valuable valuable) {
        if (!valuable.isSectionPresent("template") || !valuable.isSectionPresent("options"))
            throw new IllegalStateException("Invalid Object Generator. Missing sections...");

        variableMap.newVariable("prisoner", VariableHelper.createNullVariable(SPrisoner.class));
        initializeMap();

        template = new LadderTemplate(valuable.getSection("template").get(), variableMap);

        // Load options
        ConfigSection options = valuable.getSection("options").get();
        this.options = GeneratorOptions.of(options, variableMap);

        options.ifSectionPresent("specific", specificsSection -> {
            for (ConfigSection specificSection : specificsSection.getSections().values()) {
                LadderTemplate clone = template.clone();
                specificSection.ifValuePresent("commands", List.class, cmds -> clone.getCommands().addAll(cmds));

                // Check for message
                if (specificSection.isSectionPresent("message")) {
                    clone.setMessage(YamlMessage.load(specificSection.getSection("message").get()));

                } else if (specificSection.isValuePresent("message"))
                    clone.setMessage(new OChatMessage(specificSection.getAs("message", String.class)));

                specificSection.ifSectionPresent("requirements", reqSection -> {
                    clone.getRequirements().getHoldingData().addAll(RequirementController.initializeRequirementsSection(reqSection, variableMap).getHoldingData());
                });

                specificCache.put(this.options.getIndex(specificSection.getKey()), clone);
            }
        });

        template.initialize(variableMap);
        for (LadderTemplate value : specificCache.values())
            value.initialize(variableMap);
    }

    public boolean isValid(Object object) {
        return options.isValid(options.getIndex(object));
    }

    public boolean hasNext(Object object) {
        return options.hasNext(options.getIndex(object));
    }

    public Optional<ParsedObject> getParsed(SPrisoner prisoner, Object ob) {
        BigInteger level = options.getIndex(ob);

        Map<BigInteger, ParsedObject> cache = parsedCache.get(prisoner.getUUID());
        ParsedObject object;
        if (cache != null) {
            object = cache.get(level);
            if (object != null)
                return Optional.of(object);
        }

        if (cache == null) {
            cache = new HashMap<>();
            parsedCache.put(prisoner.getUUID(), cache);
        }

        ParsedObject parsed = parse(prisoner, level);
        cache.put(level, parsed);
        return Optional.ofNullable(parsed);
    }

    public LadderTemplate getTemplate(BigInteger key) {
        LadderTemplate template = specificCache.get(key);
        if (template != null) return template;

        return this.template;
    }

    @Override
    public Optional<Function<SPrisoner, ParsedObject>> getParser(Object key) {
        BigInteger index = getIndex(key);
        LadderTemplate template = getTemplate(index);
        if (template == null) return Optional.empty();

        return Optional.of(prisoner -> getParsed(prisoner, index).get());
    }

    protected abstract ParsedObject parse(SPrisoner prisoner, BigInteger index);

    protected abstract void initializeMap();
}
