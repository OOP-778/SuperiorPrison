package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.requirement.DeclinedRequirement;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.script.util.PasteHelper;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;
import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class PrestigeUpCmd extends OCommand {
    public PrestigeUpCmd() {
        label("prestigeup");
        description("Prestige up one time");
        alias("pup");
        onCommand(command -> {
            Player player = command.getSenderAsPlayer();
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);

            BigInteger currentPrestige = prisoner.getPrestige();
            BigInteger maxIndex = SuperiorPrisonPlugin.getInstance().getPrestigeController().getMaxIndex();

            // If prisoner is max prestige, return
            if (NumberUtil.equals(currentPrestige, maxIndex)) {
                LocaleEnum
                        .PRISONER_MAX_PRESTIGE
                        .getWithErrorPrefix()
                        .send(command.getSender());
                return;
            }

            if (LadderHelper.isRunningLadderCmd(prisoner.getUUID()))
                return;

            List<String> commands = new ArrayList<>();
            LadderHelper.addPeopleRunningLadderCmd(prisoner.getUUID());

            // Rewards executor
            Runnable commandsExecutor = () -> StaticTask.getInstance().sync(() -> {
                SuperiorPrisonPlugin.getInstance().getPlayerChatFilterController().filter(prisoner.getPlayer().getUniqueId());
                for (String s : LadderHelper.mergeCommands(commands))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);

                // Because packets are sent async, we've got to add a delay here.
                new OTask()
                        .delay(TimeUnit.SECONDS, Math.max(4, commands.size() / 100))
                        .sync(false)
                        .runnable(() -> SuperiorPrisonPlugin.getInstance().getPlayerChatFilterController().unfilter(prisoner.getOfflinePlayer().getUniqueId()))
                        .execute();
            });

            ParsedObject nextPrestige = SuperiorPrisonPlugin.getInstance().getPrestigeController().getParsed(prisoner, currentPrestige.add(BigInteger.ONE)).get();
            if (!NumberUtil.equals(SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex(), prisoner.getLadderRank()))
                commands.addAll(
                        LadderHelper.doMaxRank(
                                prisoner,
                                prisoner.getLadderRank(),
                                SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex(),
                                (ParsedObject) prisoner.getParsedLadderRank().getNext().get()
                        ).getCommands()
                );

            if (!NumberUtil.equals(SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex(), prisoner.getLadderRank())) {
                ParsedObject nextRank = (ParsedObject) prisoner.getParsedLadderRank().getNext().get();
                listedBuilder(DeclinedRequirement.class)
                        .message(LocaleEnum.RANKUP_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
                        .addObject(nextRank.getTemplate().getRequirements().meets(nextRank.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                        .addPlaceholderObject(nextRank)
                        .identifier("{TEMPLATE}")
                        .send(command);
                LadderHelper.removeFromRunningLadderCmd(prisoner.getUUID());
                commandsExecutor.run();
                return;
            }

            if (!nextPrestige.getMeets().get()) {
                listedBuilder(DeclinedRequirement.class)
                        .message(LocaleEnum.PRESTIGE_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
                        .addObject(nextPrestige.getTemplate().getRequirements().meets(nextPrestige.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                        .addPlaceholderObject(nextPrestige)
                        .identifier("{TEMPLATE}")
                        .send(command);
                LadderHelper.removeFromRunningLadderCmd(prisoner.getUUID());
                return;
            }

            nextPrestige.take();
            prisoner._setPrestige(nextPrestige.getIndex());

            if (SuperiorPrisonPlugin.getInstance().getMainConfig().isResetRanks())
                prisoner._setLadderRank(BigInteger.valueOf(1));

            prisoner.save(true);

            messageBuilder(LocaleEnum.PRESTIGE_SUCCESSFUL.getWithPrefix())
                    .replace("{prestige_name}", nextPrestige.getName())
                    .send(command);

            LadderHelper.removeFromRunningLadderCmd(prisoner.getUUID());
        });
    }
}
