package com.bgsoftware.superiorprison.api.requirement;

import lombok.Getter;

@Getter
public class RequirementException extends Exception {

  private RequirementData data;
  private Object currentValue;
  private Object required;

  public RequirementException(RequirementData data, Object currentValue) {
    this.data = data;
    this.currentValue = currentValue;
    this.required = data.getValue();
  }
}
