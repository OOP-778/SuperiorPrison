package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.script.ScriptEngine;
import com.bgsoftware.superiorprison.plugin.util.script.function.Function;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;
import org.bukkit.entity.Player;

public class CmdEval extends OCommand {
    public CmdEval() {
        label("eval");
        description("Evaluate a expression");
        ableToExecute(Player.class);
        argument(new StringArg().setGrabAllNextArgs(true).setIdentity("expression").setRequired(true));
        onCommand(command -> {
            try {
                String exp = command.getArgAsReq("expression");
                GlobalVariableMap map = new GlobalVariableMap();
                SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(command.getSenderAsPlayer());
                map.newOrPut("prisoner", () -> VariableHelper.createVariable(prisoner));

                exp = map.initializeVariables(exp, null);
                System.out.println(exp);

                Function<?> function = ScriptEngine.getInstance().initializeFunction(exp, map);

                long start = System.nanoTime();
                Object execute = function.execute(map);

                System.out.println(execute.getClass());
                command.getSenderAsPlayer().sendMessage("Took: " + (System.nanoTime() - start) + "ns, Result: " + execute);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
