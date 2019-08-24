package com.bgsoftware.superiorprison.api.data.mine;

import java.time.Instant;

public interface IMineGenerator {

    Instant getLastReset();

    Instant getNextReset();

}
