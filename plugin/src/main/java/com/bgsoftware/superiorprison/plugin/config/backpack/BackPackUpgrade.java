package com.bgsoftware.superiorprison.plugin.config.backpack;

import static com.oop.orangeengine.main.Engine.getEngine;

import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.requirement.LoadingRequirementData;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.ConfigSection;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class BackPackUpgrade {
  private final List<RequirementData> requirements = new ArrayList<>();
  private final BackPackConfig config;
  private List<String> description = new ArrayList<>();

  public BackPackUpgrade(ConfigSection section, BackPackConfig config) {
    this.config = config;

    section.ifValuePresent("description", List.class, desc -> this.description = desc);
    section.ifValuePresent(
        "requirements",
        List.class,
        requirements -> {
          for (Object o : requirements) {
            if (o.toString().trim().length() == 0) continue;

            OPair<String, RequirementData> data =
                SuperiorPrisonPlugin.getInstance().getRequirementController().parse(o.toString());
            this.requirements.add(data.getSecond());

            if (data.getSecond() instanceof LoadingRequirementData) {
              getEngine()
                  .getLogger()
                  .printWarning(
                      "Requirement by id {} is not found, converting the requirement to loading requirement...",
                      data.getSecond().getType());
              ((LoadingRequirementData) data.getSecond())
                  .setOnLoad(
                      reqData -> {
                        this.requirements.remove(data.getSecond());
                        this.requirements.add(reqData);
                      });
            }
          }
        });
  }
}
