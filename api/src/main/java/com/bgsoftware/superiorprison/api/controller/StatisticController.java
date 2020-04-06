package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.statistic.StatisticContainer;

import java.util.Optional;
import java.util.UUID;

public interface StatisticController {

    Optional<StatisticContainer> getContainer(UUID uuid);

}
