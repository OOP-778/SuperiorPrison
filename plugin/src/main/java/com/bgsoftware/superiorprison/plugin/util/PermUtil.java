package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.data.cache.OCache;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PermUtil {
    private static final OCache<UUID, List<String>> permCache = OCache
            .builder()
            .concurrencyLevel(1)
            .expireAfter(10, TimeUnit.SECONDS)
            .build();

    public static List<String> getPermissions(Pattern pattern, Player player) {
        List<String> perms = permCache.get(player.getUniqueId());
        if (perms != null) return perms;

        perms = player
                .getEffectivePermissions()
                .stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(p -> pattern.matcher(p).find())
                .collect(Collectors.toList());

        permCache.put(player.getUniqueId(), perms);
        return perms;
    }
}
