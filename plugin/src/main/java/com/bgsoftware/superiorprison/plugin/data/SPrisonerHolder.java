package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.PrisonerHolder;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.DatabaseController;
import com.oop.orangeengine.database.DatabaseHolder;
import com.oop.orangeengine.database.DatabaseObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class SPrisonerHolder implements DatabaseHolder<UUID, SPrisoner>, PrisonerHolder {

    private Map<UUID, SPrisoner> prisonerMap = Maps.newConcurrentMap();
    private DatabaseController controller;

    public SPrisonerHolder(DatabaseController controller) {
        this.controller = controller;
    }

    @Override
    public void onAdd(SPrisoner prisoner, boolean b) {
        prisonerMap.put(prisoner.getUUID(), prisoner);
    }

    @Override
    public void onRemove(SPrisoner prisoner) {
        prisonerMap.remove(prisoner.getUUID());
    }

    @Override
    public Stream<SPrisoner> dataStream() {
        return prisonerMap.values().stream();
    }

    @Override
    public UUID generatePrimaryKey(SPrisoner sPrisoner) {
        return sPrisoner.getOfflinePlayer().getUniqueId();
    }

    @Override
    public Set<Class<? extends DatabaseObject>> getObjectVariants() {
        return Sets.newHashSet(SPrisoner.class);
    }

    @Override
    public DatabaseController getDatabaseController() {
        return controller;
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
}
