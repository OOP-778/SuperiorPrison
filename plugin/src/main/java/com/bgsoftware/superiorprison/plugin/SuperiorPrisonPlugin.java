package com.bgsoftware.superiorprison.plugin;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.plugin.controller.DataController;
import com.bgsoftware.superiorprison.plugin.controller.TaskController;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.main.plugin.EnginePlugin;

public class SuperiorPrisonPlugin extends EnginePlugin implements SuperiorPrison {

    private static SuperiorPrisonPlugin instance;
    public static boolean debug = true;

    private TaskController taskController;
    private DataController dataController;
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
        //this.dataController = new DataController();
        this.taskController = new TaskController();
        new com.bgsoftware.superiorprison.plugin.SuperiorListener();
    }

    @Override
    public void disable() {
        instance = null;
    }

    @Override
    public DataController getMineController() {
        return dataController;
    }

    @Override
    public DataController getPrisonerController() {
        return dataController;
    }

    public static SuperiorPrisonPlugin getInstance() {
        return instance;
    }
}
