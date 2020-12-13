package com.bgsoftware.superiorprison.plugin.hook.impl.placeholder;

import com.bgsoftware.superiorprison.plugin.util.ExpireableCache;
import com.oop.orangeengine.main.util.data.cache.OCache;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class PlaceholderCache {
    private static final List<Predicate<String>> rules = new ArrayList<>();

    private static final OCache<UUID, ExpireableCache<String, String>> cache = OCache
            .builder()
            .concurrencyLevel(1)
            .resetExpireAfterAccess(true)
            .expireAfter(10, TimeUnit.SECONDS)
            .build();

    static {
        rules.add(input -> StringUtils.contains(input, "prisoner"));
    }

    public static boolean isCacheable(String placeholder) {
        for (Predicate<String> rule : rules) {
            if (rule.test(placeholder))
                return true;
        }
        return false;
    }

    public static Optional<String> request(UUID uuid, String placeholder) {
        ExpireableCache<String, String> userCache = cache.getIfAbsent(uuid, ExpireableCache::new);
        String s = userCache.get(placeholder);

        return Optional.ofNullable(s);
    }

    public static void put(UUID uuid, String placeholder, String value, long expireAfter) {
        ExpireableCache<String, String> userCache = cache.getIfAbsent(uuid, ExpireableCache::new);
        userCache.put(placeholder, value, expireAfter);
    }
}
