package com.bgsoftware.superiorprison.plugin.commands.mines.link;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.google.common.base.Preconditions;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdLink extends OCommand {
    public CmdLink() {
        label("link");
        description("Link settings of other mine");
        argument(new MinesArg().setIdentity("what").setRequired(true));
        argument(new MinesArg().setIdentity("to").setRequired(true));
        argument(new OptionArg().setRequired(true));
        onCommand(command -> {
            SNormalMine what = command.getArgAsReq("what");
            SNormalMine to = command.getArgAsReq("to");
            Option option = command.getArgAsReq("option");

            if (option == Option.ALL) {
                for (LinkableObject value : what.getLinkableObjects().values())
                    to.getLinker().link(what, value);
                messageBuilder(LocaleEnum.MINE_LINK_SUCCESS.getWithPrefix())
                        .replace("{what}", what.getName())
                        .replace("{to}", to.getName())
                        .replace("{option}", TextUtil.beautify(option.name()))
                        .send(command);
                return;
            }

            LinkableObject linkableObject = what.getLinkableObjects().get(option.name().toLowerCase());
            Preconditions.checkArgument(linkableObject != null, "Linking mine option is not available!");

            to.getLinker().link(what, linkableObject);
            messageBuilder(LocaleEnum.MINE_LINK_SUCCESS.getWithPrefix())
                    .replace("{what}", what.getName())
                    .replace("{to}", to.getName())
                    .replace("{option}", TextUtil.beautify(option.name()))
                    .send(command);
        });
    }
}
