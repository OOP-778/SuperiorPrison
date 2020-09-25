package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class RequirementController {
    private final Map<Class, Requirement> requirementMap = new ConcurrentHashMap<>();
    private final Map<String, Set<AtomicReference<RequirementData>>> listeners = new HashMap<>();

    @SneakyThrows
    public void registerRequirement(Requirement requirement) {
        Constructor<?> constructor = OSimpleReflection.getConstructor(requirement.getDataClass(), ConfigSection.class);
        Preconditions.checkArgument(constructor != null, "Failed to register requirement by id " + requirement.getId() + " cause it's data class doesn't have a constructor!");

        SuperiorPrisonPlugin.getInstance().getOLogger().print("[{}] Requirement registered", requirement.getId());
        requirementMap.put(requirement.getClass(), requirement);

        Set<Map.Entry<String, Set<AtomicReference<RequirementData>>>> entries = listeners.entrySet();
        for (Map.Entry<String, Set<AtomicReference<RequirementData>>> entry : entries) {
            if (entry.getKey().equalsIgnoreCase(requirement.getId())) {
                for (AtomicReference<RequirementData> ar : entry.getValue()) {
                    ConfigSection section = ((WaitingRequirementData) ar.get()).getSection();
                    ar.set((RequirementData) constructor.newInstance(section));
                }
                entries.remove(entry);
                break;
            }
        }
    }

    public void listenForRegister(String id, AtomicReference<RequirementData> ar) {
        listeners.computeIfAbsent(id, key -> Sets.newConcurrentHashSet()).add(ar);
    }

    public List<AtomicReference<RequirementData>> initializeRequirementsSection(ConfigSection requirementsSection) {
        List<AtomicReference<RequirementData>> data = new LinkedList<>();
        for (ConfigSection reqSection : requirementsSection.getSections().values()) {
            reqSection.ensureHasValues("type");

            AtomicReference<RequirementData> ar = new AtomicReference<>();

            String type = reqSection.getAs("type");
            Optional<Requirement> foundRequirement = requirementMap.values().stream().filter(req -> req.getId().equalsIgnoreCase(type)).findFirst();
            if (!foundRequirement.isPresent()) {
                ar.set(new WaitingRequirementData(reqSection));
                listenForRegister(type, ar);

            } else
                ar.set(initializeData(reqSection, foundRequirement.get()));

            data.add(ar);
        }

        return data;
    }

    @SneakyThrows
    private RequirementData initializeData(ConfigSection section, Requirement requirement) {
        return (RequirementData) OSimpleReflection.getConstructor(requirement.getDataClass(), ConfigSection.class).newInstance(section);
    }
}
