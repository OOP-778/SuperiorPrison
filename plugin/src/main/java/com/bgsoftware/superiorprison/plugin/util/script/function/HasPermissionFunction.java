package com.bgsoftware.superiorprison.plugin.util.script.function;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.ladder.LadderTemplate;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HasPermissionFunction implements Function<Boolean> {
    public static final Pattern PATTERN = Pattern
            .compile("([0-9]+)V has permission '([^ ]+)'");

    private int playerId;
    private String permission;

    @Override
    public void initialize(String string, GlobalVariableMap variableMap) {
        Matcher matcher = PATTERN.matcher(string);
        matcher.find();

        String permission = matcher.group(2);
        String _playerId = matcher.group(1);

        this.permission = permission;
        permission = variableMap.initializeVariables(permission, LadderTemplate.data);
        playerId = VariableHelper.getVariable(_playerId, variableMap).getId();
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public Boolean execute(GlobalVariableMap globalVariables) {
        GlobalVariableMap.VariableData _fromWho = globalVariables.getVariableDataById(playerId);
        Object who = _fromWho.getVariable().get(globalVariables);

        String parsedPermission = globalVariables.extractVariables(permission);
        Preconditions.checkArgument(who instanceof Player || who instanceof Prisoner, "Failed to find a valid player by variable id: " + playerId);

        Player p;
        if (who instanceof Prisoner)
            p = ((Prisoner) who).getPlayer();
        else
            p = (Player) who;

        return p.hasPermission(parsedPermission);
    }

    @Override
    public String getId() {
        return "HasPermissionCondition";
    }
}
