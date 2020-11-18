package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.requirement.DeclinedRequirement;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.StaticTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigInteger;
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

            BigInteger wasPrestige = prisoner.getPrestige();
            AtomicReference<BigInteger> currentPrestige = new AtomicReference<>(prisoner.getPrestige());
            BigInteger maxIndex = SuperiorPrisonPlugin.getInstance().getPrestigeController().getMaxIndex();

            // If prisoner is max prestige, return
            if (NumberUtil.equals(maxIndex, wasPrestige)) {
                LocaleEnum
                        .PRISONER_MAX_PRESTIGE
                        .getWithErrorPrefix()
                        .send(command.getSender());
                return;
            }

            ParsedObject startingParsed = (ParsedObject) prisoner.getParsedPrestige().orElse(null);
            VaultHook vaultHook = SuperiorPrisonPlugin.getInstance().getHookController().findHook(() -> VaultHook.class).get();
            vaultHook.enableCacheFor(player);

            int maxLadderUpsPerTime = SuperiorPrisonPlugin.getInstance().getMainConfig().getMaxLadderUpsPerTime();

            StaticTask.getInstance().async(() -> {
                long start = System.currentTimeMillis();
                ParsedObject last = null;

                int allCount = 0;
                int pCount = 0;

                while (!NumberUtil.equals(currentPrestige.get(), maxIndex)) {
                    pCount++;
                    allCount++;
                    if (pCount == 100 || allCount == maxLadderUpsPerTime) {
                        pCount = 0;
                        System.out.println(prisoner.getPrestige().toString());
                        vaultHook.submitWithdrawCache(player);
                        vaultHook.enableCacheFor(player);

                        if (allCount == maxLadderUpsPerTime)
                            break;
                    }

                    currentPrestige.set(currentPrestige.get().add(BigInteger.ONE));
                    ParsedObject current = last == null
                            ? SuperiorPrisonPlugin.getInstance().getPrestigeController().getParsed(prisoner, currentPrestige.get()).get()
                            : (ParsedObject) last.getNext().orElse(null);
                    if (current == null) break;

                    if (!NumberUtil.equals(SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex(), prisoner.getLadderRank()))
                        LadderHelper.doMaxRank(prisoner, prisoner.getLadderRank().intValue(), SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex().intValue(), (ParsedObject) prisoner.getParsedLadderRank().getNext().get());

                    if (!NumberUtil.equals(SuperiorPrisonPlugin.getInstance().getRankController().getMaxIndex(), prisoner.getLadderRank())) {
                        if (last != null) {
                            BigInteger difference = last.getIndex().subtract(wasPrestige);
                            if (!NumberUtil.equals(difference, BigInteger.ZERO)) {
                                messageBuilder(LocaleEnum.MAX_PRESTIGE_SUCCESS_NEW.getWithPrefix())
                                        .replace("{times}", difference.toString())
                                        .replace("{starting_prestige}", startingParsed.getName())
                                        .replace("{current_prestige}", last.getName())
                                        .send(command.getSender());
                            }
                        }

                        ParsedObject nextRank = (ParsedObject) prisoner.getParsedLadderRank().getNext().get();
                        listedBuilder(DeclinedRequirement.class)
                                .message(LocaleEnum.PRESTIGE_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
                                .addObject(nextRank.getTemplate().getRequirements().meets(nextRank.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                                .addPlaceholderObject(current)
                                .identifier("{TEMPLATE}")
                                .send(command);
                        return;
                    }

                    // Check if prestige meets up to the requirements
                    if (!current.getMeets().get()) break;

                    // Take the requirements
                    current.take();

                    // Set current prestige
                    prisoner._setPrestige(current.getIndex());

                    if (SuperiorPrisonPlugin.getInstance().getMainConfig().isResetRanks())
                        prisoner._setLadderRank(BigInteger.valueOf(1));

                    last = current;
                }

                if (last == null) {
                    ParsedObject parsedObject = SuperiorPrisonPlugin.getInstance().getPrestigeController().getParsed(prisoner, prisoner.getPrestige().add(BigInteger.ONE)).get();
                    listedBuilder(DeclinedRequirement.class)
                            .message(LocaleEnum.PRESTIGE_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
                            .addObject(parsedObject.getTemplate().getRequirements().meets(parsedObject.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                            .addPlaceholderObject(parsedObject)
                            .identifier("{TEMPLATE}")
                            .send(command);
                    return;
                }

                BigInteger difference = last.getIndex().subtract(wasPrestige);
                messageBuilder(LocaleEnum.MAX_PRESTIGE_SUCCESS_NEW.getWithPrefix())
                        .replace("{times}", difference.toString())
                        .replace("{starting_prestige}", startingParsed == null ? "None" : startingParsed.getName())
                        .replace("{current_prestige}", last.getName())
                        .send(command.getSender());

                last.getNext().ifPresent(prestige -> {
                    listedBuilder(DeclinedRequirement.class)
                            .addObject(((ParsedObject) prestige).getTemplate().getRequirements().meets(((ParsedObject) prestige).getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                            .addPlaceholderObject(prestige)
                            .identifier("{TEMPLATE}")
                            .message(LocaleEnum.PRESTIGE_NEED_TILL_RANKUP_REQUIREMENTS.getMessage())
                            .send(command);
                });

                prisoner.save(true);
                vaultHook.submitWithdrawCache(player);
                Bukkit.broadcastMessage("took " + (System.currentTimeMillis() - start) + "ms");
            });
        });
    }
}
