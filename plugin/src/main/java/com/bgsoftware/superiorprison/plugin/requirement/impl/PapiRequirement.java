package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class PapiRequirement implements Requirement {

    private final RequirementHandler<Data> handler = (prisoner, data) -> PlaceholderAPI.setPlaceholders(prisoner.getPlayer(), data.getPlaceholder()).contentEquals(data.getValue());

    @Nullable
    @Override
    public Class<? extends RequirementData> getDataClazz() {
        return Data.class;
    }

    @Override
    public RequirementHandler getHandler() {
        return handler;
    }

    @Override
    public String getId() {
        return "PAPI";
    }

    public static class Data extends RequirementData {

        @Getter
        private String placeholder;

        Data(Map<String, String> data) {
            super(data);

            this.placeholder = Objects.requireNonNull(data.get("placeholder"));
        }
    }

}
