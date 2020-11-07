package com.bgsoftware.superiorprison.plugin.commands.args;

import com.google.common.collect.Lists;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

public class ClearTypeArg extends CommandArgument<String> {
    public ClearTypeArg() {
        setIdentity("type");
        setDescription("If set to ladder, it will clear only ladder access, if set to special otherwise");
        setMapper(in -> new OPair<>((in.equalsIgnoreCase("ladder") || in.equalsIgnoreCase("special")) ? in : null, "Cannot find clear type by " + in));
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> Lists.newArrayList("ladder", "special"));
    }
}
