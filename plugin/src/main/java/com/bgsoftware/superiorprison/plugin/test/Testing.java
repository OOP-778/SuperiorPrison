package com.bgsoftware.superiorprison.plugin.test;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ObjectSupplier;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.impl.ManualPrestigeGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.impl.ManualRankGenerator;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementController;
import com.bgsoftware.superiorprison.plugin.test.script.ScriptEngine;
import com.bgsoftware.superiorprison.plugin.test.script.function.Function;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.yaml.Config;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Testing {
    public static RequirementController controller;
    public static ObjectSupplier ranksGenerator;
    public static ObjectSupplier prestigeGenerator;

    public static void main(String[] args) {
        controller = new RequirementController();
        ranksGenerator = new ManualRankGenerator(new Config(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "ranks.yml")));
        prestigeGenerator = new ManualPrestigeGenerator(new Config(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "prestiges.yml")));

        SyncEvents.listen(AsyncPlayerChatEvent.class, event -> {
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer());
            String exp = "random num between {{get balance of %prisoner%}*2} and {{get balance of %prisoner%}*5}";
            GlobalVariableMap map = new GlobalVariableMap();
            map.newOrPut("prisoner", () -> VariableHelper.createVariable(prisoner));

            System.out.println("wfgawfgawgawg");
            exp = map.initializeVariables(exp, null);
            System.out.println(exp);

            Function<?> function = ScriptEngine.getInstance().initializeFunction(exp, map);

            long start = System.nanoTime();
            Object execute = function.execute(map);

            prisoner.getPlayer().sendMessage("Took: " + (System.nanoTime() - start) + "ns, Result: " + execute);
        });
    }
}
