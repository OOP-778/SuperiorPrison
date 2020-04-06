package com.bgsoftware.superiorprison.plugin.commands.mines.copy;

import com.bgsoftware.superiorprison.plugin.commands.args.CopyTypeArg;
import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShopItem;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdCopy extends OCommand {
    public CmdCopy() {
        label("copy");
        description("Copy things from one mine to another");
        argument(new MinesArg().setRequired(true).setIdentity("from"));
        argument(new MinesArg().setRequired(true).setIdentity("to"));
        argument(new CopyTypeArg().setRequired(true));

        onCommand(command -> {
            SNormalMine from = command.getArgAsReq("from");
            SNormalMine to = command.getArgAsReq("to");
            CopyType type = command.getArgAsReq("type");

            switch (type) {
                case SETTINGS:
                    SMineSettings settings = SMineSettings.from(from.getSettings());
                    to.setSettings(settings);
                    settings.attach(to);
                    break;

                case SHOP:
                    from.getShop().getItems()
                            .stream()
                            .map(item -> (SShopItem) item)
                            .map(SShopItem::from).
                            forEach(to.getShop()::addItem);
                    break;

                case ACCESS:
                    to.addRank(from.getRanks().toArray(new String[0]));
                    to.addPrestige(from.getPrestiges().toArray(new String[0]));
                    break;

                case GENERATOR:
                    to.getGenerator().getGeneratorMaterials().addAll(from.getGenerator().getGeneratorMaterials());
                    to.getGenerator().setMaterialsChanged(true);
                    break;
            }

            // Save changes made
            to.save(true);

            messageBuilder(LocaleEnum.COPIED_OPTION.getWithPrefix())
                    .replace("{from_name}", from.getName())
                    .replace("{to_name}", to.getName())
                    .replace("{option_name}", type.name().toLowerCase())
                    .send(command);
        });
    }
}
