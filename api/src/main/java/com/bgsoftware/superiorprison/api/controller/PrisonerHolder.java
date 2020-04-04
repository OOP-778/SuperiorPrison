package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;

import java.util.Optional;
import java.util.UUID;

public interface PrisonerHolder {

    Optional<Prisoner> getPrisoner(UUID uuid);

    Optional<Prisoner> getPrisoner(String username);
}
