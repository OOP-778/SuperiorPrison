package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.oop.orangeengine.main.util.OptionalConsumer;

import java.util.Optional;
import java.util.UUID;

public interface PrisonerController {

    Optional<Prisoner> getPrisoner(UUID uuid);

    Optional<Prisoner> getPrisoner(String username);


}
