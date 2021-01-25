package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.impl.chat.LineContent;

import java.util.stream.Collectors;

public class CmdInfo extends OCommand {
    public CmdInfo() {
        label("info");
        description("Get information about mine");
        argument(new MinesArg().setRequired(true));
        onCommand(command -> {
            SNormalMine mine = command.getArgAsReq("mine");
            OMessage message = LocaleEnum.MINE_INFO.getMessage().clone();
            message.replace("{mine_icon}", new LineContent(Helper.beautify(OMaterial.matchMaterial(mine.getIcon())))
                    .hoverItem()
                    .item(mine.getIcon())
                    .parent()
            );
            message.replace("{mine_name}", mine.getName());
            message.replace("{mine_prisoners}", mine.getPrisoners().size());
            message.replace("{mine_blocks}", mine.getGenerator().getBlocksInRegion());
            message.replace(
                    "{mine_materials}",
                    mine.getGenerator().getGeneratorMaterials()
                            .stream()
                            .map(pair -> "(" + pair.getValue().name() + ", chance: " + pair.getKey() + ")")
                            .collect(Collectors.joining(", "))
            );

            message.send(command.getSender());
        });
    }
}
