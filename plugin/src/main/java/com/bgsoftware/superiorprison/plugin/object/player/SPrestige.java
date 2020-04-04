package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@AllArgsConstructor
public class SPrestige implements Prestige {
    private @NonNull String name;
    private @NonNull String prefix;

    private int order;
    private List<String> commands;
    private List<String> permissions;

    private Set<RequirementData> requirements;

    @Setter
    private SPrestige nextPrestige;

    @Setter
    private SPrestige previousPrestige;

    @Override
    public Optional<Prestige> getNext() {
        return Optional.ofNullable(nextPrestige);
    }

    @Override
    public Optional<Prestige> getPrevious() { return Optional.ofNullable(previousPrestige); }
}
