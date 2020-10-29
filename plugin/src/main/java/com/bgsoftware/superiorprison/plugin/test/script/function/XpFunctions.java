package com.bgsoftware.superiorprison.plugin.test.script.function;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.XPUtil;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.getVariable;

public class XpFunctions {
    public static final Pattern GET_XP_PATTERN = Pattern.compile("get (xp|xp level) of (%([^ ]+)%|([0-9]+)V)");
    public static final Pattern SET_XP_LEVEL_PATTERN = Pattern.compile("set (xp|xp level) of (%([^ ]+)%|([0-9]+)V) to ([0-9]+V|[0-9]+)");

    public static class SET_XP implements Function<Boolean> {

        private int typeId;
        private int ofId;
        private int toId;

        @Override
        public void initialize(String string, GlobalVariableMap variableMap) {
            Matcher matcher = SET_XP_LEVEL_PATTERN.matcher(string);
            matcher.find();

            String _type = matcher.group(1);
            String _of = matcher.group(2);
            String _to = matcher.group(3);

            // Initialize amount
            typeId = _type.equalsIgnoreCase("xp") ? 1 : 2;
            ofId = getVariable(_of, variableMap).getId();
            toId = getVariable(_to, variableMap).getId();
        }

        @Override
        public Class<Boolean> getType() {
            return Boolean.class;
        }

        @Override
        public boolean isCacheable() {
            return false;
        }

        @Override
        public Boolean execute(GlobalVariableMap globalVariables) {
            GlobalVariableMap.VariableData _of = globalVariables.getVariableDataById(ofId);
            GlobalVariableMap.VariableData _to = globalVariables.getVariableDataById(toId);

            Object who = _of.getVariable().get(globalVariables);
            Object to = _to.getVariable().get(globalVariables);

            Preconditions.checkArgument(who instanceof Player || who instanceof Prisoner, "Failed to find a valid player by variable id: " + ofId);
            Preconditions.checkArgument(to instanceof Number, "Failed to find valid number by id: " + to);

            Player player;
            if (who instanceof Prisoner) {
                Preconditions.checkArgument(((Prisoner) who).isOnline(), "Failed to modify xp values of " + ((Prisoner) who).getOfflinePlayer().getName() + " cause they're offline!");
                player = ((Prisoner) who).getPlayer();
            } else
                player = (Player) who;

            if (typeId == 1)
                XPUtil.setTotalExperience(player, ((Number) to).intValue());
            else
                XPUtil.setTotalExperience(player, XPUtil.getExpAtLevel(((Number) to).intValue()));

            return true;
        }

        @Override
        public String getId() {
            return "set xp";
        }
    }

    public static class GET_XP_FUNCTION implements Function<Integer> {
        private int typeId;
        private int ofId;

        @Override
        public void initialize(String string, GlobalVariableMap variableMap) {
            Matcher matcher = GET_XP_PATTERN.matcher(string);
            matcher.find();

            String _type = matcher.group(1);
            String _of = matcher.group(2);

            // Initialize amount
            typeId = _type.equalsIgnoreCase("xp") ? 1 : 2;
            ofId = getVariable(_of, variableMap).getId();
        }

        @Override
        public Class<Integer> getType() {
            return Integer.class;
        }

        @Override
        public boolean isCacheable() {
            return true;
        }

        @Override
        public Integer execute(GlobalVariableMap globalVariables) {
            GlobalVariableMap.VariableData _of = globalVariables.getVariableDataById(ofId);

            Object who = _of.getVariable().get(globalVariables);
            Preconditions.checkArgument(who instanceof Player || who instanceof Prisoner, "Failed to find a valid player by variable id: " + ofId);

            Player player;
            if (who instanceof Prisoner) {
                Preconditions.checkArgument(((Prisoner) who).isOnline(), "Failed to modify xp values of " + ((Prisoner) who).getOfflinePlayer().getName() + " cause they're offline!");
                player = ((Prisoner) who).getPlayer();
            } else
                player = (Player) who;

            if (typeId == 1)
                return XPUtil.getTotalExperience(player);
            else
                return player.getLevel();
        }

        @Override
        public String getId() {
            return "get xp";
        }
    }
}
