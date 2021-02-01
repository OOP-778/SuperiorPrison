package com.bgsoftware.superiorprison.api.requirement;

import javax.annotation.Nullable;

public interface Requirement {

  /*
  Get Requirement Data Class Used when initializing a requirement in rank, prestiges
  If null handler will be provided by default data
  */
  @Nullable
  default Class<? extends RequirementData> getDataClazz() {
    return null;
  }

  /*
  Your implementation of handling the requirement.
  Cannot be null
  */
  RequirementHandler getHandler();

  /*
  The Id of the requirement
  It's how SP identifies an requirement from config
  */
  String getId();
}
