package com.bgsoftware.superiorprison.plugin.commands.eco;

import com.bgsoftware.superiorprison.api.data.account.EconomyAccount;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.OffPlayerArg;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdBalance extends OCommand {
    public CmdBalance() {
        label("balance");
        alias("bal");
        ableToExecute(Player.class);
        argument(new OffPlayerArg().setRequired(false));
        onCommand(command -> {
            OfflinePlayer target = command
                    .getArg("player", OfflinePlayer.class)
                    .orElse(command.getSenderAsPlayer());

            EconomyAccount account = SuperiorPrisonPlugin.getInstance().getEconomyController().getAccountByUUID(target.getUniqueId());
            if (command.getSenderAsPlayer().getUniqueId().equals(account.getUUID())) {
                messageBuilder(LocaleEnum.BALANCE_SELF.getWithPrefix())
                        .replace("{money}", account.getBalance().toPlainString())
                        .send(command);
                return;
            }

            messageBuilder(LocaleEnum.BALANCE_OTHER.getWithPrefix())
                    .replace("{money}", account.getBalance().toPlainString())
                    .replace("{target}", account.getUsername())
                    .send(command);
        });
    }
}
