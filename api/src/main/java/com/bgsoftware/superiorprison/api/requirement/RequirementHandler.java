package com.bgsoftware.superiorprison.api.requirement;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;

public interface RequirementHandler<O extends RequirementData> {

    boolean test(Prisoner prisoner, O o);

    default void take(Prisoner prisoner, O o) {
    }

}
