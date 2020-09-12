package com.bgsoftware.superiorprison.plugin.commands.mines.link;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.google.common.base.Preconditions;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdUnlink extends OCommand {
    public CmdUnlink() {
        label("unlink");
        description("Unlink settings of other mine");
        argument(new MinesArg().setIdentity("what").setRequired(true));
        argument(new MinesArg().setIdentity("from").setRequired(true));
        argument(new OptionArg().setRequired(true));
        onCommand(command -> {
            SNormalMine what = command.getArgAsReq("what");
            SNormalMine from = command.getArgAsReq("from");
            Option option = command.getArgAsReq("option");

            if (option == Option.ALL) {
                for (LinkableObject value : what.getLinkableObjects().values())
                    from.getLinker().unlink(value.getLinkId(), what.getName());
                messageBuilder(LocaleEnum.MINE_UNLINK_SUCCESS.getWithPrefix())
                        .replace("{what}", what.getName())
                        .replace("{from}", from.getName())
                        .replace("{option}", TextUtil.beautify(option.name()))
                        .send(command);
                return;
            }

            LinkableObject linkableObject = what.getLinkableObjects().get(option.name().toLowerCase());
            Preconditions.checkArgument(linkableObject != null, "Linking mine option is not available!");

            from.getLinker().unlink(linkableObject.getLinkId(), what.getName());
            messageBuilder(LocaleEnum.MINE_UNLINK_SUCCESS.getWithPrefix())
                    .replace("{what}", what.getName())
                    .replace("{from}", from.getName())
                    .replace("{option}", TextUtil.beautify(option.name()))
                    .send(command);
        });
    }
}