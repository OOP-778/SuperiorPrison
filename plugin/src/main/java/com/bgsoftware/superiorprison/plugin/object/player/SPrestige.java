package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
public class SPrestige implements Prestige, Access {
  @Getter private @NonNull final String name;
  @Getter private @NonNull final String prefix;

  @Getter private final int order;

  @Getter private final List<String> commands;

  @Getter private final List<String> permissions;

  @Getter private final Set<RequirementData> requirements;

  @Setter private SPrestige nextPrestige;

  @Setter private SPrestige previousPrestige;

  @Override
  public Optional<Prestige> getNext() {
    return Optional.ofNullable(nextPrestige);
  }

  @Override
  public Optional<Prestige> getPrevious() {
    return Optional.ofNullable(previousPrestige);
  }

  @Override
  public List<Prestige> getAllPrevious() {
    List<Prestige> prestiges = new ArrayList<>();
    getPrevious()
        .map(prestige -> (SPrestige) prestige)
        .ifPresent(prestige -> prestige._getAllPrevious(prestiges));
    return prestiges;
  }

  private void _getAllPrevious(List<Prestige> prestiges) {
    prestiges.add(this);
    getPrevious()
        .map(prestige -> (SPrestige) prestige)
        .ifPresent(prestige -> prestige._getAllPrevious(prestiges));
  }
}
