package com.bgsoftware.superiorprison.plugin.hook.impl.placeholder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.placeholders.PlaceholderParser;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.ObjectCache;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PapiHook extends SHook {
    public PapiHook() {
        super(null);
        new Expansion();
    }

    public String parse(Object object, String text) {
        return parse(object instanceof SPrisoner ? ((SPrisoner) object).getOfflinePlayer() : object instanceof OfflinePlayer ? (OfflinePlayer) object : null, text);
    }

    public List<String> parse(Object object, List<String> lore) {
        return parse(object instanceof SPrisoner ? ((SPrisoner) object).getOfflinePlayer() : object instanceof OfflinePlayer ? (OfflinePlayer) object : null, lore);
    }

    public String parse(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    private List<String> parse(OfflinePlayer player, List<String> lore) {
        return PlaceholderAPI.setPlaceholders(player, lore);
    }

    @Override
    public String getPluginName() {
        return "PlaceholderAPI";
    }

    public void disable() {
        PlaceholderAPI.unregisterPlaceholderHook("prison");
    }

    public class Expansion extends PlaceholderExpansion {
        public Expansion() {
            register();
        }

        @Override
        public String getIdentifier() {
            return "prison";
        }

        @Override
        public String getAuthor() {
            return "BG-Software";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public String onPlaceholderRequest(Player p, String params) {
            return onRequest(p, params);
        }

        @Override
        public String onRequest(OfflinePlayer p, String params) {
            Optional<String> request = PlaceholderCache.request(p.getUniqueId(), params);
            if (request.isPresent())
                return request.get();

            String[] split = params.split("_");
            ObjectCache cache = new ObjectCache();
            SuperiorPrisonPlugin.getInstance().getPrisonerController()
                    .getPrisoner(p.getUniqueId())
                    .ifPresent(cache::add);

            String parse = PlaceholderParser.parse(split, cache);
            PlaceholderCache.put(p.getUniqueId(), params, parse, 5);
            return parse;
        }

        @Override
        public boolean persist() {
            return true;
        }
    }
}
