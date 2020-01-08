package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.requirement.DefaultParsedData;
import com.google.common.collect.Maps;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class RequirementController implements com.bgsoftware.superiorprison.api.controller.RequirementController {

    private Map<String, Requirement> requirements = Maps.newHashMap();

    public Optional<Requirement> findRequirement(String id) {
        return Optional.ofNullable(requirements.get(id.toUpperCase()));
    }

    @Override
    public void registerRequirement(Requirement req) {
        this.requirements.put(req.getId().toUpperCase(), req);
        SuperiorPrisonPlugin.getInstance().getOLogger().print("Requirement (" + req.getId() + ") handler registered!");
    }

    public OPair<String, Optional<RequirementData>> parse(String toParse) {
        Map<String, String> allData = new HashMap<>();
        String allSplit[] = toParse.split("}");

        String value = allSplit[1];
        allData.put("value", value.startsWith(" ") ? value.substring(1) : value);

        String splitPart1 = allSplit[0];
        if (splitPart1.startsWith(" "))
            splitPart1 = splitPart1.substring(1);

        String dataSplit[] = splitPart1.split(",");
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

        AtomicReference<RequirementData> data = new AtomicReference<>(null);
        findRequirement(allData.get("type")).ifPresent(req -> {
            if (req.getDataClazz() == null)
                data.set(new DefaultParsedData(allData));

            else {
                try {
                    Constructor<? extends RequirementData> constr = req.getDataClazz().getDeclaredConstructor(Map.class);
                    constr.setAccessible(true);

                    data.set(constr.newInstance(allData));
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });

        return new OPair<>(allData.get("type"), Optional.ofNullable(data.get()));
    }
}
