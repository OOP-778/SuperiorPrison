package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;

import java.util.Optional;
import java.util.UUID;

public interface PrisonerHolder {

    // Get a prisoner by UUID
    Optional<Prisoner> getPrisoner(UUID uuid);

    // Get a prisoner by username
    Optional<Prisoner> getPrisoner(String username);
}
