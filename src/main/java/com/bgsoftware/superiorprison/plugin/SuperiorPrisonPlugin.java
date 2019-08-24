package com.bgsoftware.superiorprison.plugin;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.plugin.controller.MineController;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.main.plugin.EnginePlugin;

public class SuperiorPrisonPlugin extends EnginePlugin implements SuperiorPrison {

    private MineController mineController;
    private ODatabase database;

    @Override
    public void enable() {

        // Setup API
        new SuperiorPrisonAPI(this);

        // Setup Database
        // TODO

        // Initialize controllers
        this.mineController = new MineController(database);

    }

    @Override
    public MineController getMineController() {
        return mineController;
    }
}
