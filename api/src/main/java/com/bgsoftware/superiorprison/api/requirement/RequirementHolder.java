package com.bgsoftware.superiorprison.api.requirement;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.util.Pair;

import java.util.List;

public interface RequirementHolder {

    // Check if the requirements meet
    Pair<Boolean, List<DeclinedRequirement>> meets(Prisoner prisoner);

    // Take the requirements from prisoner
    void take(Prisoner prisoner);

}
