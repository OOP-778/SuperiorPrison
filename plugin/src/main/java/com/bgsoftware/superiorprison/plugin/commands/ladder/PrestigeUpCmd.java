package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.requirement.DeclinedRequirement;
import com.bgsoftware.superiorprison.plugin.util.script.util.PasteHelper;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

import java.util.Optional;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;
import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class PrestigeUpCmd extends OCommand {
    public PrestigeUpCmd() {
        label("prestigeup");
        description("Prestige up one time");
        alias("pup");
        onCommand(command -> {
//            Player player = command.getSenderAsPlayer();
//            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);
//
//            int currentPrestige = prisoner.getPrestige();
//            int maxIndex = SuperiorPrisonPlugin.getInstance().getPrestigeController().getMaxIndex();
//
//            // If prisoner is max prestige, return
//            if (currentPrestige == maxIndex) {
//                LocaleEnum
//                        .PRISONER_MAX_PRESTIGE
//                        .getWithErrorPrefix()
//                        .send(command.getSender());
//                return;
//            }
//
//            Optional<ParsedObject> previousPrestige = prisoner.getParsedPrestige().map(o -> (ParsedObject) o);
//            ParsedObject nextPrestige = SuperiorPrisonPlugin.getInstance().getPrestigeController().getParsed(prisoner, currentPrestige + 1).get();
//
//            if (!nextPrestige.getMeets().get()) {
//                listedBuilder(DeclinedRequirement.class)
//                        .message(LocaleEnum.PRESTIGE_NEED_TILL_RANKUP_REQUIREMENTS.getMessage().clone())
//                        .addObject(nextPrestige.getTemplate().getRequirements().meets(nextPrestige.getVariableMap()).getSecond().toArray(new DeclinedRequirement[0]))
//                        .addPlaceholderObject(nextPrestige)
//                        .identifier("{TEMPLATE}")
//                        .send(command);
//                return;
//            }
//
//            System.out.println(PasteHelper.paste(nextPrestige.getVariableMap()));
//
//            nextPrestige.take();
//            prisoner._setPrestige(nextPrestige.getIndex());
//
//            prisoner.save(true);
//
//            messageBuilder(LocaleEnum.PRESTIGE_SUCCESSFUL.getWithPrefix())
//                    .replace("{prestige_name}", nextPrestige.getName())
//                    .send(command);
        });
    }
}
