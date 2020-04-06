package com.bgsoftware.superiorprison.plugin.object.statistic;

import com.bgsoftware.superiorprison.api.data.statistic.StatisticContainer;
import com.oop.orangeengine.database.DatabaseObject;
import com.oop.orangeengine.database.annotation.Column;
import com.oop.orangeengine.database.annotation.PrimaryKey;
import com.oop.orangeengine.database.annotation.Table;
import lombok.Getter;

import java.util.UUID;

@Table(name = "statistics")
public class SStatisticContainer extends DatabaseObject implements StatisticContainer {

    @Getter
    @PrimaryKey(name = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "blocks")
    private SBlocksStatistic blocksStatistic = new SBlocksStatistic();
}
