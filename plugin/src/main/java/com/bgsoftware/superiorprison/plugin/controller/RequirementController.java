package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.requirement.DefaultParsedData;
import com.bgsoftware.superiorprison.plugin.requirement.LoadingRequirementData;
import com.google.common.collect.Maps;
import com.oop.orangeengine.main.util.data.pair.OPair;
import io.netty.util.internal.ConcurrentSet;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class RequirementController implements com.bgsoftware.superiorprison.api.controller.RequirementController {

    private final Map<String, Requirement> requirements = Maps.newHashMap();
    private final Set<LoadingRequirementData> loadingRequirementDataSet = new ConcurrentSet<>();

    public Optional<Requirement> findRequirement(String id) {
        return Optional.ofNullable(requirements.get(id.toUpperCase()));
    }

    @Override
    public void registerRequirement(Requirement req) {
        this.requirements.put(req.getId().toUpperCase(), req);
        SuperiorPrisonPlugin.getInstance().getOLogger().print("Requirement (" + req.getId() + ") handler registered!");
        checkRequirements(req);
    }

    public void registerLoadingRequirement(LoadingRequirementData data) {
        loadingRequirementDataSet.add(data);
    }

    @SneakyThrows
    public void checkRequirements(Requirement req) {
        for (LoadingRequirementData data : loadingRequirementDataSet) {
            if (!data.getType().equalsIgnoreCase(req.getId())) return;

            Constructor<? extends RequirementData> constr = req.getDataClazz().getDeclaredConstructor(Map.class);
            constr.setAccessible(true);

            data.load(constr.newInstance(data.getDataMap()));
        }
    }

    public OPair<String, RequirementData> parse(String toParse) {
        Map<String, String> allData = new HashMap<>();
        String[] allSplit = toParse.split("}");

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

        AtomicReference<RequirementData> data = new AtomicReference<>(null);
        Optional<Requirement> type = findRequirement(allData.get("type"));
        if (type.isPresent()) {
            Requirement req = type.get();
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
        } else
            data.set(new LoadingRequirementData(allData, null));

        return new OPair<>(allData.get("type"), data.get());
    }
}
