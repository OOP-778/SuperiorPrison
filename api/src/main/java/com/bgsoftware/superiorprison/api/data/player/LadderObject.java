package com.bgsoftware.superiorprison.api.data.player;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface LadderObject {
    String getPrefix();

    String getName();

    BigInteger getIndex();

    List<String> getCommands();

    Optional<LadderObject> getNext();

    Optional<LadderObject> getPrevious();

    void take();

    void executeCommands();
}
