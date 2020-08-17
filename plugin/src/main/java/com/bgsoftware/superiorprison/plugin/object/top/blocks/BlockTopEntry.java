package com.bgsoftware.superiorprison.plugin.object.top.blocks;

import com.bgsoftware.superiorprison.api.data.top.TopEntry;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.statistic.SBlocksStatistic;
import com.bgsoftware.superiorprison.plugin.object.top.STopEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class BlockTopEntry extends STopEntry<SBlocksStatistic> {
    public BlockTopEntry(SPrisoner prisoner, SBlocksStatistic object, int position) {
        super(prisoner, object, position);
    }
}
