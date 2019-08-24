package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.IMineController;
import com.bgsoftware.superiorprison.api.data.mine.ISuperiorMine;
import com.bgsoftware.superiorprison.plugin.object.mine.NormalMine;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.object.DataController;

import java.util.Set;

public class MineController extends DataController implements IMineController {
    public MineController(ODatabase database) {
        super(database);
        registerClass(NormalMine.class);
        load();
    }

    @Override
    public Set<ISuperiorMine> getMines() {
        return getData(ISuperiorMine.class);
    }
}
