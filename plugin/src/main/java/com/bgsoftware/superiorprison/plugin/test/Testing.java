package com.bgsoftware.superiorprison.plugin.test;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.test.generator.ObjectSupplier;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.generator.PrestigeGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.impl.ManualPrestigeGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.impl.ManualRankGenerator;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementController;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.yaml.Config;

public class Testing {
    public static RequirementController controller;
    public static ObjectSupplier ranksGenerator;
    public static ObjectSupplier prestigeGenerator;

    public static void main(String[] args) {
        controller = new RequirementController();
        ranksGenerator = new ManualRankGenerator(new Config(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "ranks.yml")));
        prestigeGenerator = new PrestigeGenerator(new Config(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "prestiges.yml")));
        //prestigeGenerator = new ManualPrestigeGenerator(new Config(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "prestiges.yml")));
    }
}
