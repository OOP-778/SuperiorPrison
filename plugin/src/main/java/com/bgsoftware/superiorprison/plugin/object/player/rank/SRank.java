package com.bgsoftware.superiorprison.plugin.object.player.rank;

import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
@Getter
public class SRank implements Rank {
    private @NonNull String name;
    private @NonNull String prefix;

    private List<String> commands;

    private List<String> permissions;
}
