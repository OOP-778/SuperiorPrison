package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

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

            ParsedObject nextRank = (ParsedObject) prisoner.getParsedLadderRank().getNext().get();
            if (!nextRank.getMeets().get()) {

                return;
            }

            nextRank.take();
        });
    }
}
