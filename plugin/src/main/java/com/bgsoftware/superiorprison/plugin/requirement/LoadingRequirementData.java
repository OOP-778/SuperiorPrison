package com.bgsoftware.superiorprison.plugin.requirement;

import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class LoadingRequirementData extends RequirementData {

  private boolean loaded = false;

  @Setter private Consumer<RequirementData> onLoad;

  public LoadingRequirementData(Map<String, String> data, Consumer<RequirementData> onLoad) {
    super(data);
    this.onLoad = onLoad;
    SuperiorPrisonPlugin.getInstance().getRequirementController().registerLoadingRequirement(this);
  }

  public void load(@NonNull RequirementData data) {
    Preconditions.checkArgument(
        onLoad != null, "Failed to load an LoadingRequirement because onLoad is null!");
    this.onLoad.accept(data);
    loaded = true;
  }
}
