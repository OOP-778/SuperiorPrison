package com.bgsoftware.superiorprison.plugin.test;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.access.MineCondition;
import com.bgsoftware.superiorprison.plugin.object.mine.access.SMineAccess;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ObjectSupplier;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.impl.ManualPrestigeGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.impl.ManualRankGenerator;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementController;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.Helper;
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
            if (!prisoner.getCurrentMine().isPresent()) return;

            Pair<SuperiorMine, AreaEnum> pair = prisoner.getCurrentMine().get();
            SMineAccess access = (SMineAccess) pair.getKey().getAccess();
            for (MineCondition condition : access.getConditions()) {
                event.getPlayer().sendMessage(Helper.color(condition.getName() + " : " + condition.getPlainString()));
                event.getPlayer().sendMessage(access.canEnterDebug(prisoner) + "");
            }
        });
    }
}
