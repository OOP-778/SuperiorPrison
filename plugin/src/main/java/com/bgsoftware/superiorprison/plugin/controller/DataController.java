package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.MineController;
import com.bgsoftware.superiorprison.api.controller.PrisonerController;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.main.util.OptionalConsumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class DataController extends com.oop.orangeengine.database.object.DataController implements PrisonerController, MineController {
    public DataController(ODatabase database) {
        super(database);

        registerClass(SPrisoner.class);
        registerClass(SNormalMine.class);

        //setAutoSave(true);
        load();
    }

    @Override
    public Set<SuperiorMine> getMines() {
        return getData(SuperiorMine.class);
    }

    @Override
    public Optional<SuperiorMine> getMine(String mineName) {
        return getMines().stream().filter(mine -> mine.getName().equalsIgnoreCase(mineName)).findFirst();
    }

    public OptionalConsumer<SuperiorMine> getMineFiltered(Predicate<SuperiorMine> predicate) {
        return OptionalConsumer.of(getMines().stream().filter(predicate).findFirst());
    }

    @Override
    public Optional<SuperiorMine> getMineAt(Location location) {
        return getMines().stream().filter(mine -> mine.isInside(location)).findFirst();
    }

    public Set<String> getMinesWorlds() {
        Set<String> worlds = new HashSet<>();
        getMines().forEach(mine -> {
            if (!worlds.contains(mine.getMinPoint().worldName()))
                worlds.add(mine.getHighPoint().worldName());
        });

        return worlds;
    }

    @Override
    public Optional<Prisoner> getPrisoner(UUID uuid) {
        return getData(Prisoner.class).stream()
                .filter(prisoner -> prisoner.getUUID() == uuid)
                .findFirst();
    }

    @Override
    public Optional<Prisoner> getPrisoner(String username) {
        return getData(Prisoner.class).stream()
                .filter(prisoner -> prisoner.getOfflinePlayer().getName().contentEquals(username))
                .findFirst();
    }

    public SPrisoner insertOrGetPrisoner(Player player) {

        Optional<Prisoner> optionalPrisoner = getPrisoner(player.getUniqueId());
        if (optionalPrisoner.isPresent())
            return (SPrisoner) optionalPrisoner.get();

        else {
            SPrisoner prisoner = new SPrisoner(player.getUniqueId());
            getData().add(prisoner);

            return prisoner;
        }

    }

}
