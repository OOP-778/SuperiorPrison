package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;

@AllArgsConstructor
public class SPrestige implements Prestige, Access {
    @Getter
    private
    @NonNull String name;
    @Getter
    private @NonNull String prefix;

    @Getter
    private int order;

    @Getter
    private List<String> commands;

    @Getter
    private List<String> permissions;

    @Getter
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
