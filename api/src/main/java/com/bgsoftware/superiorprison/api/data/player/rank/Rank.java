package com.bgsoftware.superiorprison.api.data.player.rank;

import java.util.List;

public interface Rank {
    String getPrefix();

    String getName();

    List<String> getPermissions();
}

