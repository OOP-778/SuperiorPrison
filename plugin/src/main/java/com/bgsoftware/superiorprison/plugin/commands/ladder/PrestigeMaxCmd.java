package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.requirement.DeclinedRequirement;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;
import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class PrestigeMaxCmd extends OCommand {
    public PrestigeMaxCmd() {
        label("prestigemax");
        alias("pmax");
        ableToExecute(Player.class);
        description("Prestige up to the max available prestige!");
        onCommand(command -> {
            Player player = command.getSenderAsPlayer();

            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);
            if (LadderHelper.isRunningLadderCmd(prisoner.getUUID())) return;

            BigInteger wasPrestige = prisoner.getPrestige();
            AtomicReference<BigInteger> currentPrestige = new AtomicReference<>(prisoner.getPrestige());
            BigInteger maxIndex = SuperiorPrisonPlugin.getInstance().getPrestigeController().getMaxIndex();

            // If player has cooldown, return...
            if (!LadderHelper.checkForCooldown(player))
                return;

            // If prisoner is max prestige, return
            if (NumberUtil.equals(maxIndex, wasPrestige)) {
                LocaleEnum
                        .PRISONER_MAX_PRESTIGE
                        .getWithErrorPrefix()
                        .send(command.getSender());
                return;
            }

            final List<String>[] commands = new List[]{new ArrayList<>()};
            LadderHelper.addPeopleRunningLadderCmd(prisoner.getUUID());

            // Rewards executor
            Runnable commandsExecutor = () -> {
                commands[0] = LadderHelper.mergeCommands(commands[0]);
                StaticTask.getInstance().sync(() -> {
                    SuperiorPrisonPlugin.getInstance().getPlayerChatFilterController().filter(prisoner.getPlayer().getUniqueId());
                    for (String s : commands[0]) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                    }

                    // Because packets are sent async, we've got to add a delay here.
                    new OTask()
                            .delay(TimeUnit.SECONDS, Math.max(4, commands[0].size() / 100))
                            .sync(false)
                            .runnable(() -> SuperiorPrisonPlugin.getInstance().getPlayerChatFilterController().unfilter(prisoner.getOfflinePlayer().getUniqueId()))
                            .execute();
                });
            };

            ParsedObject startingParsed = (ParsedObject) prisoner.getParsedPrestige().orElse(null);

            int maxLadderUpsPerTime = SuperiorPrisonPlugin.getInstance().getMainConfig().getMaxLadderUpsPerTime();
            StaticTask.getInstance().async(() -> {
                ParsedObject last = null;

                int allCount = 0;
                int pCount = 0;

                boolean breakCauseOfLimit = false;
                while (!NumberUtil.equals(currentPrestige.get(), maxIndex)) {
                    pCount++;
                    allCount++;
                    currentPrestige.set(currentPrestige.get().add(BigInteger.ONE));

                    ParsedObject current = last == null
                            ? SuperiorPrisonPlugin.getInstance().getPrestigeController().getParsed(prisoner, currentPrestige.get()).get()
                            : (ParsedObject) last.getNext().orElse(null);
                    if (current == null) break;

                    if (!NumberUtil.equals(SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex(), prisoner.getLadderRank()))
                        LadderHelper.doMaxRank(
                                prisoner,
                                prisoner.getLadderRank(),
                                SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex(),
                                (ParsedObject) prisoner.getParsedLadderRank().getNext().get()
                        );

                    if (!NumberUtil.equals(SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex(), prisoner.getLadderRank())) {
                        if (last != null) {
                            BigInteger difference = last.getIndex().subtract(wasPrestige);
                            if (!NumberUtil.equals(difference, BigInteger.ZERO)) {
                                messageBuilder(LocaleEnum.MAX_PRESTIGE_SUCCESS_NEW.getWithPrefix())
                                        .replace("{times}", difference.toString())
                                        .replace("{starting_prestige}", startingParsed.getName())
                                        .replace("{current_prestige}", last.getName())
                                        .replace("{starting_prestige_formatted}", NumberUtil.formatBigInt(startingParsed.getIndex()))
                                        .replace("{current_prestige_formatted}", NumberUtil.formatBigInt(last.getIndex()))
                                        .send(command.getSender());
                            }
                        }

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

                    // Check if prestige meets up to the requirements
                    if (!current.getMeets().get())
                        break;

                    // Take the requirements
                    current.take();

                    // Execute the commands
                    commands[0].addAll(current.getCommands());

                    // Set current prestige
                    prisoner._setPrestige(current.getIndex());

                    if (SuperiorPrisonPlugin.getInstance().getMainConfig().isResetRanks())
                        prisoner._setLadderRank(BigInteger.valueOf(1));

                    last = current;
                    if (pCount == 100 || allCount == maxLadderUpsPerTime) {
                        pCount = 0;

                        if (allCount == maxLadderUpsPerTime) {
                            breakCauseOfLimit = true;
                            break;
                        }
                    }
                }

                if (last == null) {
                    ParsedObject parsedObject = SuperiorPrisonPlugin.getInstance().getPrestigeController().getParsed(prisoner, prisoner.getPrestige().add(BigInteger.ONE)).get();
                    listedBuilder(DeclinedRequirement.class)
                            .message(LocaleEnum.PRESTIGE_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
                            .addObject(parsedObject.getTemplate().getRequirements().meets(parsedObject.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                            .addPlaceholderObject(parsedObject)
                            .identifier("{TEMPLATE}")
                            .send(command);
                    LadderHelper.removeFromRunningLadderCmd(prisoner.getUUID());
                    return;
                }

                BigInteger difference = last.getIndex().subtract(wasPrestige);
                messageBuilder(LocaleEnum.MAX_PRESTIGE_SUCCESS_NEW.getWithPrefix())
                        .replace("{times}", difference.toString())
                        .replace("{starting_prestige}", startingParsed == null ? "None" : startingParsed.getName())
                        .replace("{current_prestige}", last.getName())
                        .replace("{starting_prestige_formatted}", startingParsed == null ? "None" : NumberUtil.formatBigInt(startingParsed.getIndex()))
                        .replace("{current_prestige_formatted}", NumberUtil.formatBigInt(last.getIndex()))
                        .send(command.getSender());

                if (!breakCauseOfLimit)
                    last.getNext().ifPresent(prestige -> {
                        listedBuilder(DeclinedRequirement.class)
                                .addObject(((ParsedObject) prestige).getTemplate().getRequirements().meets(((ParsedObject) prestige).getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                                .addPlaceholderObject(prestige)
                                .identifier("{TEMPLATE}")
                                .message(LocaleEnum.PRESTIGE_NEED_TILL_RANKUP_REQUIREMENTS.getMessage())
                                .send(command);
                    });
                else {
                    long cooldown = SuperiorPrisonPlugin.getInstance().getMainConfig().getMaxLadderUpsCooldown();
                    messageBuilder(LocaleEnum
                            .PRISONER_LADDERUP_REACHED_LIMIT
                            .getWithErrorPrefix()
                    )
                            .replace("{limit}", maxLadderUpsPerTime)
                            .replace("{cooldown}", TimeUtil.toString(cooldown))
                            .send(player);

                    LadderHelper.cooldown(player.getUniqueId(), cooldown);
                }

                prisoner.save(true);
                commandsExecutor.run();
                LadderHelper.removeFromRunningLadderCmd(prisoner.getUUID());
            });
        });
    }
}
