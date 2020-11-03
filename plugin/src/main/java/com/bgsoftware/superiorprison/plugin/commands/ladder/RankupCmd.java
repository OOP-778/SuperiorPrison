package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.requirement.DeclinedRequirement;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;
import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class RankupCmd extends OCommand {
    public RankupCmd() {
        label("rankup");
        alias("rup");
        ableToExecute(Player.class);
        onCommand(command -> {
            Player player = command.getSenderAsPlayer();
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);

            int currentRank = prisoner.getLadderRank();
            int maxIndex = Testing.ranksGenerator.getMaxIndex();

            // If prisoner is max rank, return
            if (currentRank == maxIndex) {
                LocaleEnum
                        .RANKUP_MAX
                        .getWithErrorPrefix()
                        .send(command.getSender());
                return;
            }

            ParsedObject previousRank = (ParsedObject) prisoner.getParsedLadderRank();
            ParsedObject nextRank = (ParsedObject) previousRank.getNext().get();
            if (!nextRank.getMeets().get()) {
                listedBuilder(DeclinedRequirement.class)
                        .message(LocaleEnum.RANKUP_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
                        .addObject(nextRank.getTemplate().getRequirements().meets(nextRank.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                        .addPlaceholderObject(nextRank)
                        .identifier("{TEMPLATE}")
                        .send(command);
                return;
            }

            nextRank.take();
            prisoner._setLadderRank(nextRank.getIndex());

            prisoner.save(true);

            messageBuilder(LocaleEnum.RANKUP_SUCCESSFUL.getWithPrefix())
                    .replace("{previous_rank}", previousRank.getName())
                    .replace("{current_rank}", nextRank.getName())
                    .send(command);
        });
    }
}
