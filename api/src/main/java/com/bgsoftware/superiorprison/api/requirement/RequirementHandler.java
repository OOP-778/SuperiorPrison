package com.bgsoftware.superiorprison.api.requirement;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;

public interface RequirementHandler<O extends RequirementData> {

  boolean testIO(Prisoner prisoner, O o) throws RequirementException;

  default boolean test(Prisoner prisoner, O o) {
    try {
      return testIO(prisoner, o);
    } catch (RequirementException ex) {
      return false;
    }
  }

  default void take(Prisoner prisoner, O o) {}

  int getPercentage(Prisoner prisoner, O o);

  String getCurrent(Prisoner prisoner, O o);
}
