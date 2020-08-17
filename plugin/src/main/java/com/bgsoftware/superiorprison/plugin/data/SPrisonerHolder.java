package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.PrisonerHolder;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Maps;
import com.oop.datamodule.database.TableEditor;
import com.oop.datamodule.storage.SqlStorage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class SPrisonerHolder extends SqlStorage<SPrisoner> implements PrisonerHolder {

    @Getter
    private final Map<UUID, SPrisoner> prisonerMap = Maps.newConcurrentMap();

    @Getter
    private final Map<String, UUID> usernameToUuidMap = Maps.newConcurrentMap();

    public SPrisonerHolder(DatabaseController controller) {
        super(controller, controller.getDatabase());

        new TableEditor("prisoners")
                .renameColumn("prestiges", "currentPrestige")
                .addColumn("currentLadderRank", "TEXT")
                .edit(controller.getDatabase());

        SuperiorPrisonPlugin.getInstance().getRankController().addLoadHook(c -> {
            for (SPrisoner prisoner : getPrisonerMap().values()) {
                // Update the ladder rank
                LadderRank currentLadderRank = prisoner.getCurrentLadderRank();
                Optional<LadderRank> ladderRank = c.getLadderRank(currentLadderRank.getName());
                ladderRank.ifPresent(rank -> prisoner.setLadderRank(rank, false));
            }
        });

        SuperiorPrisonPlugin.getInstance().getPrestigeController().addLoadHook(c -> {
            for (SPrisoner prisoner : getPrisonerMap().values()) {
                // Update the prestige
                Optional<Prestige> currentPrestige = prisoner.getCurrentPrestige();
                if (currentPrestige.isPresent()) {
                    Optional<Prestige> prestige = c.getPrestige(currentPrestige.get().getOrder());
                    prestige.ifPresent(p -> prisoner.setPrestige(p, false));
                }
            }
        });
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

    public Stream<SPrisoner> streamOnline() {
        return stream().filter(SPrisoner::isOnline);
    }

    @Override
    public Optional<Prisoner> getPrisoner(UUID uuid) {
        return Optional.ofNullable(prisonerMap.get(uuid));
    }

    @Override
    public Optional<Prisoner> getPrisoner(String username) {
        UUID uuid = usernameToUuidMap.get(username);
        if (uuid == null) {
            if (username.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"))
                return getPrisoner(UUID.fromString(username));

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
            if (offlinePlayer == null) return Optional.empty();
            uuid = offlinePlayer.getUniqueId();
            usernameToUuidMap.put(username, uuid);
        }

        return getPrisoner(uuid);
    }

    public SPrisoner getInsertIfAbsent(Player player) {
        return getInsertIfAbsent(player.getUniqueId());
    }

    private SPrisoner newPrisoner(SPrisoner prisoner) {
        add(prisoner);
        return prisoner;
    }

    @Override
    public Iterator<SPrisoner> iterator() {
        return prisonerMap.values().iterator();
    }

    public SPrisoner getInsertIfAbsent(UUID uuid) {
        Optional<Prisoner> optionalPrisoner = getPrisoner(uuid);
        return optionalPrisoner.map(prisoner -> (SPrisoner) prisoner).orElseGet(() -> newPrisoner(new SPrisoner(uuid)));
    }
}
