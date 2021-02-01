package com.bgsoftware.superiorprison.api.requirement;

import java.util.Map;

public abstract class RequirementData {

  private boolean take = true;
  private String value;
  private String type;

  private Map<String, String> data;

  public RequirementData(Map<String, String> data) {
    this.value = data.get("value");
    this.data = data;
    this.type = data.get("type");

    String take = data.get("take");
    if (take != null) this.take = Boolean.parseBoolean(take);
  }

  public boolean isTake() {
    return take;
  }

  public String getValue() {
    return value;
  }

  public Map<String, String> getDataMap() {
    return data;
  }

  public String getType() {
    return type;
  }
}
