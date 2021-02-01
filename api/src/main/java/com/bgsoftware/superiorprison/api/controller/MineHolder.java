package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Location;

public interface MineHolder {
  /*
  Get all mines inside the database
  */
  Set<SuperiorMine> getMines();

  /*
  Get mine by specific name
  */
  Optional<SuperiorMine> getMine(String mineName);

  /*
  Get mine at specific location
  */
  Optional<SuperiorMine> getMineAt(Location location);
}
