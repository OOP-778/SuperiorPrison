package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.MineController;
import com.bgsoftware.superiorprison.api.controller.PrisonerController;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.database.ODatabase;

import java.util.HashSet;
import java.util.Set;

public class DataController extends com.oop.orangeengine.database.object.DataController implements PrisonerController, MineController {
    public DataController(ODatabase database) {
        super(database);

        registerClass(SPrisoner.class);
        registerClass(SNormalMine.class);
    }

    @Override
    public Set<SuperiorMine> getMines() {
        return getData(SuperiorMine.class);
    }

    public Set<String> getMinesWorlds() {
        Set<String> worlds = new HashSet<>();
        getMines().forEach(mine -> {
            if (!worlds.contains(mine.getMinPoint().getWorldName()))
                worlds.add(mine.getHighPoint().getWorldName());
        });

        return worlds;
    }
}
