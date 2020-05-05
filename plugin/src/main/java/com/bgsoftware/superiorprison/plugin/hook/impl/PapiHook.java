package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PapiHook extends SHook {
    public PapiHook() {
        super(null);
        new Expansion();
    }

    @Override
    public String getPluginName() {
        return "PlaceholderAPI";
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

    public List<String> parse(OfflinePlayer player, List<String> lore) {
        return PlaceholderAPI.setPlaceholders(player, lore);
    }

    private String[] cutArray(String[] array, int amount) {
        if (array.length <= amount)
            return new String[0];
        else
            return Arrays.copyOfRange(array, amount, array.length);
    }

    public String getFromLocation(Location location, String identifier) {
        if (identifier.equalsIgnoreCase("world"))
            return location.getWorld().getName();

        else if (identifier.equalsIgnoreCase("x"))
            return location.getBlockX() + "";

        else if (identifier.equalsIgnoreCase("y"))
            return location.getBlockY() + "";

        else if (identifier.equalsIgnoreCase("z"))
            return location.getBlockZ() + "";

        else
            return "invalid identifier";
    }

    public String getFromAccess(Object object, String identifier) {
        if (identifier == null)
            return object instanceof Rank ? ((Rank) object).getName() : ((Prestige) object).getName();

        if (identifier.equalsIgnoreCase("prefix"))
            return object instanceof Rank ? ((Rank) object).getPrefix() : ((Prestige) object).getPrefix();

        if (identifier.equalsIgnoreCase("order"))
            return object instanceof LadderRank ? ((LadderRank) object).getOrder() + "" : ((Prestige) object).getOrder() + "";

        return "invalid identifier";
    }

    public class Expansion extends PlaceholderExpansion {
        public Expansion() {
            register();
        }

        @Override
        public String getIdentifier() {
            return "superiorprison";
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
            if (split[0].equalsIgnoreCase("prisoner") && split.length >= 2) {
                SPrisoner prisoner;
                if (p == null) {
                    String prisonerIdentifier = split[1];
                    if (prisonerIdentifier.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"))
                        prisoner = (SPrisoner) SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(UUID.fromString(prisonerIdentifier)).orElse(null);

                    else
                        prisoner = (SPrisoner) SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(prisonerIdentifier).orElse(null);
                } else {
                    prisoner = (SPrisoner) SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(p.getUniqueId()).orElse(null);
                    split = addToArray(split, 1, "");
                }
                if (prisoner == null) return "Invalid prisoner";

                if (split[2].equalsIgnoreCase("autosell"))
                    return prisoner.isAutoSell() ? "enabled" : "disabled";

                else if (split[2].equalsIgnoreCase("currentmine")) {
                    Optional<Pair<SuperiorMine, AreaEnum>> currentMine = prisoner.getCurrentMine();
                    if (currentMine.isPresent()) {
                        if (split.length > 3 && split[2].equalsIgnoreCase("area"))
                            return currentMine.get().getValue().name().toLowerCase();

                        return currentMine.get().getKey().getName();
                    } else
                        return "None";

                } else if (split[2].equalsIgnoreCase("autopickup"))
                    return prisoner.isAutoPickup() ? "enabled" : "disabled";

                else if (split[2].equalsIgnoreCase("autoburn"))
                    return prisoner.isAutoBurn() ? "enabled" : "disabled";

                else if (split[2].equalsIgnoreCase("fortuneblocks"))
                    return prisoner.isFortuneBlocks() ? "enabled" : "disabled";

                else if (split[2].equalsIgnoreCase("ladderrank")) {
                    LadderRank currentLadderRank = prisoner.getCurrentLadderRank();
                    if (split.length >= 4) {
                        if (split[3].equalsIgnoreCase("previous") || split[3].equalsIgnoreCase("next")) {
                            LadderRank obj = split[3].equalsIgnoreCase("previous") ? currentLadderRank.getPrevious().orElse(null) : currentLadderRank.getNext().orElse(null);
                            if (obj == null) return "none";

                            if (split.length == 5)
                                return getFromAccess(obj, split[4]);

                            else
                                return obj.getName();
                        } else
                            return getFromAccess(currentLadderRank, split[3]);

                    } else
                        return currentLadderRank.getName();

                } else if (split[2].equalsIgnoreCase("prestige")) {
                    Prestige prestige = prisoner.getCurrentPrestige().orElse(null);
                    if (prestige == null) return "none";

                    if (split.length >= 4) {
                        if (split[3].equalsIgnoreCase("previous") || split[3].equalsIgnoreCase("next")) {
                            Prestige obj = split[3].equalsIgnoreCase("previous") ? prestige.getPrevious().orElse(null) : prestige.getNext().orElse(null);
                            if (obj == null) return "none";

                            if (split.length == 5)
                                return getFromAccess(obj, split[4]);

                            else
                                return obj.getName();
                        } else
                            return getFromAccess(prestige, split[3]);

                    } else
                        return prestige.getName();

                } else if (split[2].equalsIgnoreCase("statistic")) {
                    SStatisticsContainer statisticsContainer = SuperiorPrisonPlugin.getInstance().getStatisticsController().getContainer(prisoner.getUUID());
                    if (split[3].equalsIgnoreCase("blocks")) {
                        if (split.length == 5 && split[4].equalsIgnoreCase("total")) {
                            return statisticsContainer.getBlocksStatistic().getTotal() + "";
                        }
                    }
                }
            } else if (split[0].equalsIgnoreCase("mine") && split.length > 2) {
                SNormalMine mine = (SNormalMine) SuperiorPrisonPlugin.getInstance().getMineController().getMine(split[1]).orElse(null);
                if (mine == null) return "invalid mine";

                if (split[2].equalsIgnoreCase("type"))
                    return mine.getType().name().toLowerCase();

                else if (split[2].equalsIgnoreCase("spawnpoint"))
                    if (split.length == 4)
                        return getFromLocation(mine.getSpawnPoint(), split[3]);

                    else
                        return "none";

                else if (split[2].equalsIgnoreCase("region")) {
                    if (split.length == 5) {
                        if (split[3].equalsIgnoreCase("minpoint"))
                            return getFromLocation(mine.getArea(AreaEnum.REGION).getMinPoint(), split[4]);

                        else if (split[3].equalsIgnoreCase("highpoint"))
                            return getFromLocation(mine.getArea(AreaEnum.REGION).getHighPoint(), split[4]);
                    }
                } else if (split[2].equalsIgnoreCase("minpoint")) {
                    if (split.length == 4)
                        return getFromLocation(mine.getArea(AreaEnum.MINE).getMinPoint(), split[3]);

                } else if (split[2].equalsIgnoreCase("highpoint")) {
                    if (split.length == 4)
                        return getFromLocation(mine.getArea(AreaEnum.MINE).getHighPoint(), split[3]);

                } else if (split[2].equalsIgnoreCase("prisoners"))
                    return mine.getPrisoners().size() + "";

                else if (split[2].equalsIgnoreCase("reset")) {
                    ResetSettings resetSettings = mine.getSettings().getResetSettings();
                    if (split.length == 4) {
                        if (split[3].equalsIgnoreCase("type"))
                            return resetSettings.isTimed() ? "timed" : "percentage";

                        else if (split[3].equalsIgnoreCase("percentage")) {
                            if (resetSettings.isTimed()) return "none";
                            return resetSettings.asPercentage().getValue() + "";

                        } else if (split[3].equalsIgnoreCase("timeleft")) {
                            if (!resetSettings.isTimed()) return "none";
                            return TimeUtil.leftToString(resetSettings.asTimed().getResetDate());

                        } else if (split[3].equalsIgnoreCase("currentpercentage"))
                            return mine.getGenerator().getPercentageOfFullBlocks() + "";

                        else if (split[3].equalsIgnoreCase("lastreset"))
                            return mine.getGenerator().getLastReset() == null ? "none" : TimeUtil.leftToString(TimeUtil.getDate(mine.getGenerator().getLastReset().getEpochSecond()), true);
                    }
                }
            }
            return "";
        }

        private String[] addToArray(String[] split, int index, String s) {
            String[] newArray = new String[split.length + 1];
            boolean found = false;
            for (int i = 0; i < split.length + 1; i++) {
                if (i == index) {
                    found = true;
                    newArray[i] = s;
                } else {
                    if (!found)
                        newArray[i] = split[i];
                    else
                        newArray[i] = split[i - 1];
                }
            }

            return newArray;
        }
    }
}
