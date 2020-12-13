package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.requirement.DeclinedRequirement;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import org.bukkit.entity.Player;

import java.math.BigInteger;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;
import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class RankupMaxCmd extends OCommand {
    public RankupMaxCmd() {
        label("rankupmax");
        alias("rmax");
        description("Rankup to the maximum rank you can!");
        ableToExecute(Player.class);
        onCommand(command -> {
            Player player = command.getSenderAsPlayer();
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);

            BigInteger currentRank = prisoner.getLadderRank();
            BigInteger maxIndex = SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex();

            // If prisoner is max rank, return
            if (NumberUtil.equals(currentRank, maxIndex)) {
                LocaleEnum
                        .RANKUP_MAX
                        .getWithErrorPrefix()
                        .send(command.getSender());
                return;
            }

            ParsedObject startingParsed = (ParsedObject) prisoner.getParsedLadderRank();

            StaticTask.getInstance().async(() -> {
                LadderHelper.MaxRankResult rankupResult = LadderHelper.doMaxRank(prisoner, currentRank, maxIndex, (ParsedObject) startingParsed.getNext().get());
                BigInteger ladderRankIndex = prisoner.getLadderRank();
                if (!NumberUtil.equals(ladderRankIndex, currentRank)) {
                    messageBuilder(LocaleEnum.MAX_RANKUP_SUCCESS_NEW.getWithPrefix())
                            .replace("{times}", rankupResult.getTimesRankedUp())
                            .replace("{starting_rank}", startingParsed.getName())
                            .replace("{current_rank}", rankupResult.getLast().getName())
                            .send(command.getSender());
                    prisoner.save(true);
                }

                if (!NumberUtil.equals(currentRank, maxIndex)) {
                    ParsedObject nextRank = SuperiorPrisonPlugin.getInstance().getRankController()
                            .getParsed(prisoner, ladderRankIndex.add(BigInteger.ONE))
                            .orElse(null);
                    if (nextRank == null) return;

                    listedBuilder(DeclinedRequirement.class)
                            .message(LocaleEnum.RANKUP_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
                            .addObject(nextRank.getTemplate().getRequirements().meets(nextRank.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                            .addPlaceholderObject(nextRank)
                            .identifier("{TEMPLATE}")
                            .send(command);
                }
            });
        });
    }
}
