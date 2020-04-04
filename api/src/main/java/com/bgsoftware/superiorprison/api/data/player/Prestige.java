package com.bgsoftware.superiorprison.api.data.player;

import java.util.Optional;

public interface Prestige {
    String getName();

    String getPrefix();

    int getOrder();

    Optional<Prestige> getNext();

    Optional<Prestige> getPrevious();
}
