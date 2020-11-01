package com.bgsoftware.superiorprison.api.data.player;

import java.util.List;
import java.util.Optional;

public interface LadderObject {
    String getPrefix();

    String getName();

    int getIndex();

    List<String> getPermissions();

    Optional<LadderObject> getNext();

    Optional<LadderObject> getPrevious();

    void take();
}
