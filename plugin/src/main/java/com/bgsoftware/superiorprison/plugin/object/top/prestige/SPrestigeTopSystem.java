package com.bgsoftware.superiorprison.plugin.object.top.prestige;

import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.top.STopSystem;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SPrestigeTopSystem extends STopSystem<SPrisoner, SPrestigeTopEntry> {

    @Override
    protected Comparator<SPrisoner> comparator() {
        return Comparator.comparingInt(p -> p.getParsedPrestige().map(LadderObject::getIndex).get());
    }

    @Override
    protected Stream<SPrisoner> stream() {
        return SuperiorPrisonPlugin.getInstance()
                .getPrisonerController()
                .stream();
    }

    @Override
    protected Predicate<SPrisoner> filter() {
        return prisoner -> prisoner.getParsedPrestige().isPresent();
    }

    @Override
    protected BiFunction<SPrisoner, Integer, SPrestigeTopEntry> constructor() {
        return (o, p) -> new SPrestigeTopEntry(o, o, p);
    }

    @Override
    public String getName() {
        return "Prestige";
    }
}
