package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.statistic.StatisticsContainer;

import java.util.UUID;

public interface StatisticsController {
    // Get statistics container for a prisoner
    StatisticsContainer getContainer(UUID uuid);
}
