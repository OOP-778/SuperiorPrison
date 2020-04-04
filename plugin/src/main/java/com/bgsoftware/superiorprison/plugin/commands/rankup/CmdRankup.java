package com.bgsoftware.superiorprison.plugin.commands.rankup;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

import java.util.*;

public class CmdRankup extends OCommand {
    public CmdRankup() {
        label("rankup");
        ableToExecute(Player.class);
        onCommand(command -> {
            Player player = command.getSenderAsPlayer();
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);

            LadderRank currentLadderRank = prisoner.getCurrentLadderRank();
            Optional<SLadderRank> nextOptional = currentLadderRank.getNext().map(rank -> (SLadderRank) rank);
            if (nextOptional.isPresent()) {
                SLadderRank next = nextOptional.get();

                List<RequirementException> failed = new ArrayList<>();
                next.getRequirements()
                        .forEach(data -> {
                            Optional<Requirement> requirement = SuperiorPrisonPlugin.getInstance().getRequirementController().findRequirement(data.getType());
                            try {
                                requirement.get().getHandler().testIO(prisoner, data);
                            } catch (RequirementException ex) {
                                failed.add(ex);
                            }
                        });

                // Failed to rankup cause requirements aren't met
                if (!failed.isEmpty()) {
                    LocaleEnum.RANKUP_FAILED_DOES_NOT_MEET_REQUIREMENTS.getWithErrorPrefix().send(player, ImmutableMap.of("%rank%", next.getName()));
                    failed.forEach(data -> LocaleEnum.RANKUP_REQUIREMENT_FORMAT.getMessage().send(player, ImmutableMap.of("%requirement%", data.getData().getType(), "%current%", data.getCurrentValue() + "", "%required%", data.getRequired() + "")));

                } else {
                    LocaleEnum.RANKUP_SUCCESSFUL.getWithPrefix().send(player, ImmutableMap.of("%previous_rank%", currentLadderRank.getName(), "%current_rank%", next.getName()));

                    prisoner.addRank(next);
                    prisoner.save(true);
                }
            } else
                LocaleEnum.RANKUP_MAX.getWithErrorPrefix().send(player);
        });
    }
}
