package com.bgsoftware.superiorprison.api.data.ladder;

import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.util.NumberWrapper;
import java.util.List;
import java.util.Optional;

public interface LadderObject {
  NumberWrapper getOrder();

  Optional<LadderObject> getNext();

  Optional<LadderObject> getPrevious();

  List<RequirementData> getRequirements();

  List<String> getCommands();
}
