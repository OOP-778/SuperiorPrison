package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import java.util.Optional;

public class RankRequirement implements Requirement {
  private static final RequirementHandler<RequirementData> handler =
      new RequirementHandler<RequirementData>() {
        @Override
        public boolean testIO(Prisoner prisoner, RequirementData requirementData)
            throws RequirementException {
          boolean has = prisoner.hasRank(requirementData.getValue());
          if (!has)
            throw new RequirementException(
                requirementData, prisoner.getCurrentLadderRank().getName());
          return true;
        }

        @Override
        public int getPercentage(Prisoner prisoner, RequirementData requirementData) {
          LadderRank currentLadderRank = prisoner.getCurrentLadderRank();
          Optional<LadderRank> ladderRank =
              SuperiorPrisonPlugin.getInstance()
                  .getRankController()
                  .getLadderRank(requirementData.getValue());
          if (ladderRank.isPresent()) {
            int max = ladderRank.get().getOrder();
            int current = currentLadderRank.getOrder();
            return Math.min(current * 100 / max, 100);
          }
          return 100;
        }

        @Override
        public String getCurrent(Prisoner prisoner, RequirementData requirementData) {
          return prisoner.getCurrentLadderRank().getName();
        }
      };

  @Override
  public RequirementHandler getHandler() {
    return handler;
  }

  @Override
  public String getId() {
    return "RANK";
  }
}
