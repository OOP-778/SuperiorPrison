package com.bgsoftware.superiorprison.plugin.test;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.generator.PrestigeGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.ManualObjectGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.impl.ManualRankGenerator;
import com.bgsoftware.superiorprison.plugin.test.requirement.DeclinedRequirement;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementController;
import com.bgsoftware.superiorprison.plugin.test.script.util.PasteHelper;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.yaml.Config;
import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Testing {
    public static RequirementController controller;

    public static void main(String[] args) {
        controller = new RequirementController();
        OFile file = new OFile(new File("D:\\Work\\SuperiorPrison\\plugin\\src\\main\\resources\\test\\prestiges.yml")).createIfNotExists(true);
        Config config = new Config(file);

        OChatMessage message = new OChatMessage(
                "Summary of %level% prestige",
                "Index: %level%",
                "Prefix: %prefix%",
                "Commands: %commands%",
                "Has next: %hasNext%",
                "Has Previous: %hasPrevious%",
                "Req: %req%",
                "Took: %took%"
        );

        ManualRankGenerator generator = new ManualRankGenerator(new Config(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "ranks.yml")));
        SyncEvents.listen(AsyncPlayerChatEvent.class, event -> {
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer());

            long start = System.currentTimeMillis();
            ParsedObject parsed;
            if (Values.isNumber(event.getMessage())) {
                int prestigeOrder = Values.parseAsInt(event.getMessage());
                parsed = generator.getParsed(prisoner, prestigeOrder).orElse(null);
            } else
                parsed = generator.getParsed(prisoner, event.getMessage()).orElse(null);

            if (parsed == null) {
                event.getPlayer().sendMessage("Invalid Rank");
                return;
            }

            OPair<Boolean, List<DeclinedRequirement>> meets = parsed.getTemplate().getRequirements().meets(parsed.getVariableMap());

            String req = meets.getSecond()
                    .stream()
                    .map(dr -> "req: " + dr.getDisplay() + ", required: " + dr.getRequired() + ", current: " + dr.getValue())
                    .collect(Collectors.joining(", "));

            OChatMessage clone = message.clone();
            clone
                    .replace("%level%", parsed.getLevel())
                    .replace("%prefix%", parsed.getPrefix())
                    .replace("%commands%", String.join(", ", parsed.getCommands()))
                    .replace("%hasNext%", parsed.getNext().get() != null)
                    .replace("%hasPrevious%", parsed.getPrevious().get() != null)
                    .replace("%req%", req)
                    .replace("%took%", (System.currentTimeMillis() - start))
                    .send(event.getPlayer());
        });
    }
}
