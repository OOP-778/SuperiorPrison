package com.bgsoftware.superiorprison.plugin.object.player.rank;

import com.bgsoftware.superiorprison.api.data.player.rank.SpecialRank;

import java.util.List;

public class SSpecialRank extends SRank implements SpecialRank {
    public SSpecialRank(String name, String prefix, List<String> commands, List<String> permissions) {
        super(name, prefix, commands, permissions);
    }
}
