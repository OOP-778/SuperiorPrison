package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.config.main.MainConfig;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.task.StaticTask;
import lombok.Getter;

@Getter
public class DatabaseController extends com.oop.orangeengine.database.DatabaseController {

    private SPrisonerHolder prisonerHolder;
    private SMineHolder mineHolder;

    public DatabaseController(MainConfig config) {
        setDatabase(config.getDatabase().getDatabase());

        this.prisonerHolder = new SPrisonerHolder(this);
        this.mineHolder = new SMineHolder(this);

        registerHolder(SPrisoner.class, prisonerHolder);
        registerHolder(SNormalMine.class, mineHolder);

        StaticTask.getInstance().async(this::load);
    }
}
