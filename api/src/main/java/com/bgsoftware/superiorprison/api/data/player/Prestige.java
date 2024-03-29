package com.bgsoftware.superiorprison.api.data.player;

import java.util.List;
import java.util.Optional;

public interface Prestige {
  String getName();

  String getPrefix();

  int getOrder();

  List<String> getPermissions();

  Optional<Prestige> getNext();

  Optional<Prestige> getPrevious();

  List<Prestige> getAllPrevious();
}
