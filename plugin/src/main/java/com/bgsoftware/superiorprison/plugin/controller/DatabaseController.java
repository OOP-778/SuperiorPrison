package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.config.main.MainConfig;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticContainer;
import com.oop.orangeengine.main.task.StaticTask;
import lombok.Getter;

@Getter
public class DatabaseController extends com.oop.orangeengine.database.DatabaseController {
    private SPrisonerHolder prisonerHolder;
    private SMineHolder mineHolder;
    private SStatisticHolder statisticHolder;

    public DatabaseController(MainConfig config) {
        setDatabase(config.getDatabase().getDatabase());

        this.prisonerHolder = new SPrisonerHolder(this);
        this.mineHolder = new SMineHolder(this);
        this.statisticHolder = new SStatisticHolder(this);

        registerHolder(SPrisoner.class, prisonerHolder);
        registerHolder(SNormalMine.class, mineHolder);
        registerHolder(SStatisticContainer.class, statisticHolder);

        StaticTask.getInstance().async(this::load);
    }
}
