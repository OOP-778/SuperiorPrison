package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;

public class PapiRequirement implements Requirement {
  private static final RequirementHandler<Data> handler =
      new RequirementHandler<Data>() {
        @Override
        public boolean testIO(Prisoner prisoner, Data data) throws RequirementException {
          String currentValue = getCurrent(prisoner, data);
          if (!currentValue.contentEquals(data.getValue()))
            throw new RequirementException(data, currentValue);

          return true;
        }

        @Override
        public int getPercentage(Prisoner prisoner, Data data) {
          return 100;
        }

        @Override
        public String getCurrent(Prisoner prisoner, Data data) {
          return PlaceholderAPI.setPlaceholders(prisoner.getPlayer(), data.getPlaceholder());
        }
      };

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

    @Getter private final String placeholder;

    Data(Map<String, String> data) {
      super(data);

      this.placeholder = Objects.requireNonNull(data.get("placeholder"));
    }
  }
}
