package com.bgsoftware.superiorprison.plugin.util.script.function;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.placeholder.PapiHook;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderFunction implements Function<String> {
    public static Pattern PATTERN = Pattern.compile("parse `([0-9]+)V` placeholder as ([0-9]+)V");

    private int placeholderId;
    private int playerId;

    @Override
    public void initialize(String string, GlobalVariableMap variableMap) {
        Matcher matcher = PATTERN.matcher(string);
        matcher.find();

        String _placeholder = matcher.group(1);
        String _playerId = matcher.group(2);

        placeholderId = VariableHelper.getVariable(_placeholder, variableMap).getId();
        playerId = VariableHelper.getVariable(_playerId, variableMap).getId();
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public String execute(GlobalVariableMap globalVariables) {
        GlobalVariableMap.VariableData placeholderVar = globalVariables.getVariableDataById(placeholderId);
        GlobalVariableMap.VariableData playerVar = globalVariables.getVariableDataById(playerId);

        String placeholder = placeholderVar.getInput();
        placeholder = globalVariables.extractVariables(placeholder);

        Object player = playerVar.getVariable().get(globalVariables);

        Preconditions.checkArgument(player instanceof OfflinePlayer || player instanceof Prisoner, "Failed to find a valid player by variable id: " + playerId);
        String finalPlaceholder = placeholder;
        return SuperiorPrisonPlugin.getInstance().getHookController()
                .findHook(() -> PapiHook.class)
                .map(hook -> hook.parse(player, finalPlaceholder))
                .orElse("Invalid Placeholder");
    }

    @Override
    public String getId() {
        return "PlaceholderParse";
    }
}
