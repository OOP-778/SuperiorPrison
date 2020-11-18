package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import org.bukkit.entity.Player;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class RankupMaxCmd extends OCommand {
    public RankupMaxCmd() {
        label("rankupmax");
        alias("rmax");
        description("Rankup to the maximum rank you can!");
        ableToExecute(Player.class);
        onCommand(command -> {
//            Player player = command.getSenderAsPlayer();
//            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);
//
//            int[] currentRank = new int[]{prisoner.getLadderRank()};
//            int maxIndex = SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex();
//
//            // If prisoner is max rank, return
//            if (currentRank[0] == maxIndex) {
//                LocaleEnum
//                        .RANKUP_MAX
//                        .getWithErrorPrefix()
//                        .send(command.getSender());
//                return;
//            }
//
//            ParsedObject startingParsed = (ParsedObject) prisoner.getParsedLadderRank();
//
//            StaticTask.getInstance().async(() -> {
//                OPair<ParsedObject, Integer> pair = LadderHelper.doMaxRank(prisoner, currentRank[0], maxIndex, (ParsedObject) startingParsed.getNext().get());
//
//                messageBuilder(LocaleEnum.MAX_RANKUP_SUCCESS_NEW.getWithPrefix())
//                        .replace("{times}", pair.getValue())
//                        .replace("{starting_rank}", startingParsed.getName())
//                        .replace("{current_rank}", pair.getKey().getName())
//                        .send(command.getSender());
//                prisoner.save(true);
//            });
        });
    }
}
