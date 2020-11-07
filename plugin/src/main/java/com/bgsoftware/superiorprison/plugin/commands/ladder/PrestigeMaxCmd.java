package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.requirement.DeclinedRequirement;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.StaticTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

            int wasPrestige = prisoner.getPrestige();
            int[] currentPrestige = new int[]{prisoner.getPrestige()};
            int maxIndex = Testing.prestigeGenerator.getMaxIndex();

            // If prisoner is max prestige, return
            if (currentPrestige[0] == maxIndex) {
                LocaleEnum
                        .PRISONER_MAX_PRESTIGE
                        .getWithErrorPrefix()
                        .send(command.getSender());
                return;
            }

            ParsedObject startingParsed = (ParsedObject) prisoner.getParsedPrestige().orElse(null);
            VaultHook vaultHook = SuperiorPrisonPlugin.getInstance().getHookController().findHook(() -> VaultHook.class).get();
            vaultHook.enableCacheFor(player);

            StaticTask.getInstance().async(() -> {
                long start = System.currentTimeMillis();
                ParsedObject last = null;
                while (currentPrestige[0] != maxIndex) {
                    currentPrestige[0] += 1;
                    ParsedObject current = last == null
                            ? Testing.prestigeGenerator.getParsed(prisoner, currentPrestige[0]).get()
                            : (ParsedObject) last.getNext().orElse(null);
                    if (current == null) break;

                    if (prisoner.getLadderRank() != Testing.ranksGenerator.getMaxIndex())
                        LadderHelper.doMaxRank(prisoner, prisoner.getLadderRank(), Testing.ranksGenerator.getMaxIndex(), (ParsedObject) prisoner.getParsedLadderRank().getNext().get());

                    if (prisoner.getLadderRank() != Testing.ranksGenerator.getMaxIndex()) {
                        if (last != null && (last.getIndex() - startingParsed.getIndex()) != 0) {
                            int difference = last.getIndex() - wasPrestige;
                            messageBuilder(LocaleEnum.MAX_PRESTIGE_SUCCESS_NEW.getWithPrefix())
                                    .replace("{times}", difference)
                                    .replace("{starting_prestige}", startingParsed.getName())
                                    .replace("{current_prestige}", last.getName())
                                    .send(command.getSender());
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
                    prisoner.setPrestige(current.getIndex(), true);

                    if (SuperiorPrisonPlugin.getInstance().getMainConfig().isResetRanks())
                        prisoner._setLadderRank(1);

                    last = current;
                }

                if (last == null) {
                    ParsedObject parsedObject = Testing.prestigeGenerator.getParsed(prisoner, prisoner.getPrestige() + 1).get();
                    listedBuilder(DeclinedRequirement.class)
                            .message(LocaleEnum.PRESTIGE_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
                            .addObject(parsedObject.getTemplate().getRequirements().meets(parsedObject.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
                            .addPlaceholderObject(parsedObject)
                            .identifier("{TEMPLATE}")
                            .send(command);
                    return;
                }

                int difference = last.getIndex() - wasPrestige;
                messageBuilder(LocaleEnum.MAX_PRESTIGE_SUCCESS_NEW.getWithPrefix())
                        .replace("{times}", difference)
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
