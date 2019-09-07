package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.IMineController;
import com.bgsoftware.superiorprison.api.controller.IPrisonerController;
import com.bgsoftware.superiorprison.api.data.mine.ISuperiorMine;
import com.bgsoftware.superiorprison.plugin.object.mine.NormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.Prisoner;
import com.oop.orangeengine.database.ODatabase;

import java.util.Set;

public class DataController extends com.oop.orangeengine.database.object.DataController implements IPrisonerController, IMineController {
    public DataController(ODatabase database) {
        super(database);

        registerClass(Prisoner.class);
        registerClass(NormalMine.class);
    }

    @Override
    public Set<ISuperiorMine> getMines() {
        return getData(ISuperiorMine.class);
    }
}
