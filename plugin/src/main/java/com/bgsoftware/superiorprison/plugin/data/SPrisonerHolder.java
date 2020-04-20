package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.PrisonerHolder;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.datamodule.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Stream;

public class SPrisonerHolder extends DataStorage<SPrisoner> implements PrisonerHolder {

    private Map<UUID, SPrisoner> prisonerMap = Maps.newConcurrentMap();
    private DatabaseController controller;

    public SPrisonerHolder(DatabaseController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public Class<? extends SPrisoner>[] getVariants() {
        return new Class[]{SPrisoner.class};
    }

    @Override
    public void onAdd(SPrisoner prisoner) {
        prisonerMap.put(prisoner.getUUID(), prisoner);
    }

    @Override
    public void onRemove(SPrisoner prisoner) {
        prisonerMap.remove(prisoner.getUUID());
    }

    @Override
    public Stream<SPrisoner> stream() {
        return prisonerMap.values().stream();
    }

    @Override
    public Optional<Prisoner> getPrisoner(UUID uuid) {
        return Optional.ofNullable(prisonerMap.get(uuid));
    }

    @Override
    public Optional<Prisoner> getPrisoner(String username) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
        if (offlinePlayer == null) return Optional.empty();

        return getPrisoner(offlinePlayer.getUniqueId());
    }

    public SPrisoner getInsertIfAbsent(Player player) {
        Optional<Prisoner> optionalPrisoner = getPrisoner(player.getUniqueId());
        return optionalPrisoner.map(prisoner -> (SPrisoner) prisoner).orElseGet(() -> newPrisoner(new SPrisoner(player.getUniqueId())));
    }

    private SPrisoner newPrisoner(SPrisoner prisoner) {
        add(prisoner);
        return prisoner;
    }

    @Override
    public Iterator<SPrisoner> iterator() {
        return prisonerMap.values().iterator();
    }
}
