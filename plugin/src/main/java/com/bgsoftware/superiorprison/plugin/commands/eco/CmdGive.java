package com.bgsoftware.superiorprison.plugin.commands.eco;

import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.plugin.commands.args.BigNumberArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.BoolArg;
import com.oop.orangeengine.command.arg.arguments.OffPlayerArg;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdGive extends OCommand {
    public CmdGive() {
        label("give");
        description("Give certain amount of money to people");
        argument(new OffPlayerArg().setRequired(true));
        argument(new BigNumberArg().setRequired(true));
        argument(new BoolArg().setIdentity("silent").setDescription("Do the action silently, the player won't know about it."));
        onCommand(command -> {
            OfflinePlayer player = command.getArgAsReq("player");
            BigDecimal amount = command.getArgAsReq("number");

            boolean silent = command
                    .getArg("silent", boolean.class)
                    .orElse(false);

            messageBuilder(LocaleEnum.ECONOMY_GIVE.getWithPrefix())
                    .replace("{money}", amount.toPlainString())
                    .replace("{money_formatted}", NumberUtil.formatBigDecimal(amount))
                    .replace("{target}", player.getName())
                    .send(command);

            if (!silent && player.isOnline())
                messageBuilder(LocaleEnum.ECONOMY_GIVE_TARGET.getWithPrefix())
                        .replace("{money}", amount.toPlainString())
                        .replace("{money_formatted}", NumberUtil.formatBigDecimal(amount))
                        .replace("{who}", command.getSender() instanceof Player ? command.getSenderAsPlayer().getName() : "Console")
                        .send(player.getPlayer());

            SuperiorPrisonAPI.getPlugin().getEconomyController()
                    .getAccountByUUID(player.getUniqueId())
                    .add(amount);
        });
    }
}
