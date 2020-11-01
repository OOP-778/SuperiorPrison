package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.task.StaticTask;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.entity.Player;
import org.junit.Test;

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

            command.getSender().sendMessage(maxIndex + "");
            command.getSender().sendMessage(Helper.color(prisoner.getParsedLadderRank().getName()));
            command.getSender().sendMessage(Helper.color(prisoner.getParsedPrestige().map(LadderObject::getName).orElse("None")));

            ParsedObject startingParsed = (ParsedObject) prisoner.getParsedPrestige().orElse(null);

            StaticTask.getInstance().async(() -> {
                ParsedObject last = null;
                while (currentPrestige[0] != maxIndex) {
                    currentPrestige[0] += 1;
                    ParsedObject current = last == null
                            ? Testing.prestigeGenerator.getParsed(prisoner, currentPrestige[0]).get()
                            : (ParsedObject) last.getNext().orElse(null);
                    if (current == null) break;

                    if (prisoner.getLadderRank() != Testing.ranksGenerator.getMaxIndex())
                        LadderHelper.doMaxRank(prisoner, prisoner.getLadderRank(), Testing.ranksGenerator.getMaxIndex(), (ParsedObject) prisoner.getParsedLadderRank().getNext().get());

                    if (prisoner.getLadderRank() != Testing.ranksGenerator.getMaxIndex())
                        break;

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
                if (last == null) return;

                int difference = last.getIndex() - wasPrestige;
                messageBuilder(LocaleEnum.MAX_RANKUP_SUCCESS_NEW.getWithPrefix())
                        .replace("{times}", difference)
                        .replace("{starting_prestige}", startingParsed == null ? "None" : startingParsed.getName())
                        .replace("{current_prestige}", last.getName())
                        .send(command.getSender());
                prisoner.save(true);
            });
        });
    }
}
