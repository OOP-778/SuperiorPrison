package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.util.placeholders.PlaceholderParser;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.ObjectCache;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

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
            OfflinePlayer offlinePlayer = p != null ? Bukkit.getOfflinePlayer(p.getUniqueId()) : null;
            return onRequest(offlinePlayer, params);
        }

        @Override
        public String onRequest(OfflinePlayer p, String params) {
            String[] split = params.split("_");
            ObjectCache cache = new ObjectCache();
            if (p != null)
                SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(p.getUniqueId()).ifPresent(cache::add);
            return PlaceholderParser.parse(split, cache);
        }
    }
}
