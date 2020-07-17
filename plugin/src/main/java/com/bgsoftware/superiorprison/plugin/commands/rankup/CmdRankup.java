package com.bgsoftware.superiorprison.plugin.commands.rankup;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.PermissionsInitializer;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;
import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

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
                List<RequirementException> failed = testRequirements(next.getRequirements(), prisoner);

                // Failed to rankup cause requirements aren't met
                if (!failed.isEmpty()) {
                    listedBuilder(RequirementException.class)
                            .addObject(failed.toArray(new RequirementException[0]))
                            .addPlaceholderObject(prisoner, next)
                            .identifier("{TEMPLATE}")
                            .message(LocaleEnum.RANKUP_FAILED_DOES_NOT_MEET_REQUIREMENTS.getWithErrorPrefix())
                            .send(command);

                } else {
                    messageBuilder(LocaleEnum.RANKUP_SUCCESSFUL.getWithPrefix())
                            .replace("{previous_rank}", currentLadderRank.getName())
                            .replace("{current_rank}", next.getName())
                            .send(command);

                    takeRequirements(next.getRequirements(), prisoner);
                    prisoner.setLadderRank(next, true);
                    prisoner.save(true);
                }
            } else {
                Optional<Prestige> currentPrestige = prisoner.getCurrentPrestige();
                SPrestige next = null;
                if (currentPrestige.isPresent()) {
                    Optional<Prestige> prestige = currentPrestige.get().getNext();
                    if (prestige.isPresent())
                        next = (SPrestige) prestige.get();

                } else {
                    Optional<Prestige> prestige = SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestige(1);
                    if (prestige.isPresent())
                        next = (SPrestige) prestige.get();
                }

                if (next == null) {
                    LocaleEnum.PRISONER_MAX_PRESTIGE.getWithErrorPrefix().send(player);
                    return;
                }

                List<RequirementException> failed = testRequirements(next.getRequirements(), prisoner);
                if (!failed.isEmpty()) {
                    listedBuilder(RequirementException.class)
                            .addObject(failed.toArray(new RequirementException[0]))
                            .addPlaceholderObject(prisoner, next)
                            .identifier("{TEMPLATE}")
                            .message(LocaleEnum.PRESTIGE_FAILED_DOES_NOT_MEET_REQUIREMENTS.getWithErrorPrefix())
                            .send(command);
                    return;
                }

                takeRequirements(next.getRequirements(), prisoner);

                prisoner.setPrestige(next, true);
                if (SuperiorPrisonPlugin.getInstance().getMainConfig().isResetRanks())
                    prisoner.setLadderRank(SuperiorPrisonPlugin.getInstance().getRankController().getDefault(), false);

                prisoner.save(true);

                messageBuilder(LocaleEnum.PRESTIGE_SUCCESSFUL.getWithPrefix())
                        .replace(prisoner, next)
                        .send(command);
            }
        });
        PermissionsInitializer.registerPrisonerCommand(this);
    }

    public List<RequirementException> testRequirements(Set<RequirementData> datas, SPrisoner prisoner) {
        List<RequirementException> failed = new ArrayList<>();
        datas.forEach(data -> {
            Optional<Requirement> requirement = SuperiorPrisonPlugin.getInstance().getRequirementController().findRequirement(data.getType());
            if (!requirement.isPresent()) return;
            try {
                requirement.get().getHandler().testIO(prisoner, data);
            } catch (RequirementException ex) {
                failed.add(ex);
            }
        });
        return failed;
    }

    public void takeRequirements(Set<RequirementData> datas, SPrisoner prisoner) {
        datas.forEach(data -> {
            Optional<Requirement> requirement = SuperiorPrisonPlugin.getInstance().getRequirementController().findRequirement(data.getType());
            if (!requirement.isPresent()) return;
            if (!data.isTake()) return;

            requirement.get().getHandler().take(prisoner, data);
        });
    }
}
