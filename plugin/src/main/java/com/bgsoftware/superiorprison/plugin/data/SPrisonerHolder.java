package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.PrisonerHolder;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Maps;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.commonsql.storage.SqlStorage;
import com.oop.datamodule.commonsql.util.TableEditor;
import com.oop.datamodule.universal.UniversalStorage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SPrisonerHolder extends UniversalStorage<SPrisoner> implements PrisonerHolder {
    private final Pattern uuidPattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    @Getter
    private final Map<String, UUID> usernameToUuidMap = Maps.newConcurrentMap();

    @Getter
    private final Map<UUID, SPrisoner> prisonerMap = new ConcurrentHashMap<>();

    public SPrisonerHolder(DatabaseController controller) {
        super(controller);
        addVariant("prisoners", SPrisoner.class);

        currentImplementation(
                (Storage<SPrisoner>) SuperiorPrisonPlugin.getInstance().getMainConfig().getStorageSection().getStorageProvider().apply(this)
        );

        if (getCurrentImplementation() instanceof SqlStorage) {
            new TableEditor("prisoners")
                    .renameColumn("prestiges", "currentPrestige")
                    .addColumn("currentLadderRank", "TEXT")
                    .addDropColumn("ranks")
                    .edit(((SqlStorage) getCurrentImplementation()).getDatabase());
        }

        long start = System.currentTimeMillis();
        onLoad(mine -> {
            SuperiorPrisonPlugin.getInstance().getOLogger().print(
                    "Loaded {} prisoners. Took ({}ms)",
                    prisonerMap.size(),
                    (System.currentTimeMillis() - start)
            );
        });
    }

    public Stream<SPrisoner> streamOnline() {
        return stream().filter(SPrisoner::isOnline);
    }

    @Override
    public Optional<Prisoner> getPrisoner(UUID uuid) {
        return Optional.ofNullable(getPrisonerMap().get(uuid));
    }

    @Override
    protected void onAdd(SPrisoner prisoner) {
        prisonerMap.put(prisoner.getUUID(), prisoner);
    }

    @Override
    protected void onRemove(SPrisoner prisoner) {
        prisonerMap.put(prisoner.getUUID(), prisoner);
    }

    @Override
    public Stream<SPrisoner> stream() {
        return prisonerMap.values().stream();
    }

    @Override
    public Optional<Prisoner> getPrisoner(String username) {
        UUID uuid = usernameToUuidMap.get(username);
        if (uuid == null) {
            if (uuidPattern.matcher(username).matches())
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

    public SPrisoner getInsertIfAbsent(UUID uuid) {
        Optional<Prisoner> optionalPrisoner = getPrisoner(uuid);
        return optionalPrisoner
                .map(prisoner -> (SPrisoner) prisoner)
                .orElseGet(() -> newPrisoner(new SPrisoner(uuid)));
    }

    public void initializeCache() {
        for (SPrisoner prisoner : this) {
            usernameToUuidMap.put(prisoner.getOfflinePlayer().getName(), prisoner.getUUID());
        }
    }

    public void cleanInvalids() {
        long start = System.currentTimeMillis();
        long count = stream()
                .filter(prisoner -> prisoner.getOfflinePlayer() == null || prisoner.getOfflinePlayer().getName() == null)
                .peek(this::remove)
                .count();

        SuperiorPrisonPlugin.getInstance().getOLogger().print("Prisoner Invalidation DONE ({}) Took {}ms", count, (System.currentTimeMillis() - start));
    }

    @Override
    public Iterator<SPrisoner> iterator() {
        return prisonerMap.values().iterator();
    }
}
