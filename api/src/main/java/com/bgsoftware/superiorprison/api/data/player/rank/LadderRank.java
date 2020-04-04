package com.bgsoftware.superiorprison.api.data.player.rank;

import java.util.List;
import java.util.Optional;

public interface LadderRank extends Rank {
    int getOrder();

    Optional<LadderRank> getNext();

    Optional<LadderRank> getPrevious();

    List<LadderRank> getAllPrevious();

    default boolean isDefault() {
        return getOrder() == 1;
    }
}
