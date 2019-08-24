package com.bgsoftware.superiorprison.plugin;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.plugin.controller.MineController;
import com.bgsoftware.superiorprison.plugin.controller.PrisonerController;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.main.plugin.EnginePlugin;

public class SuperiorPrisonPlugin extends EnginePlugin implements SuperiorPrison {

    private static SuperiorPrisonPlugin instance;

    private MineController mineController;
    private PrisonerController prisonerController;
    private ODatabase database;

    public SuperiorPrisonPlugin() {
        instance = this;
    }

    @Override
    public void enable() {

        // Setup API
        new SuperiorPrisonAPI(this);

        // Setup Database
        // TODO

        // Initialize controllers
        this.mineController = new MineController(database);
        this.prisonerController = new PrisonerController(database);
    }

    @Override
    public void disable() {
        instance = null;
    }

    @Override
    public MineController getMineController() {
        return mineController;
    }

    @Override
    public PrisonerController getPrisonerController() {
        return prisonerController;
    }
}
