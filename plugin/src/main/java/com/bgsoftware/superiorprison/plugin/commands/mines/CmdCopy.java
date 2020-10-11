package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.commands.args.CopyTypeArg;
import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.control.OptionEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.effects.SMineEffect;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShopItem;
import com.oop.orangeengine.command.OCommand;

import java.util.stream.Collectors;

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
            OptionEnum type = command.getArgAsReq("type");

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
                    to.getGenerator().getGeneratorMaterials().clear();
                    to.getGenerator().getGeneratorMaterials().addAll(from.getGenerator().getGeneratorMaterials());
                    to.getGenerator().setMaterialsChanged(true);
                    break;

                case ICON:
                    to.setIcon(from.getIcon().clone());
                    break;

                case FLAGS:
                    for (SArea area : to.getAreas().values()) {
                        area.getFlags().clear();
                        area.getFlags().putAll(from.getArea(area.getType()).getFlags());
                    }
                    break;

                case EFFECTS:
                    to.getEffects().clear();
                    to.getEffects().addAll(from.getEffects().get().stream().map(effect -> new SMineEffect(effect.getType(), effect.getAmplifier())).collect(Collectors.toSet()));

                case MESSAGES:
                    from.getMessages().get().stream().map(message -> ((SMineMessage) message).clone()).forEach(message -> to.getMessages().add(message));
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
