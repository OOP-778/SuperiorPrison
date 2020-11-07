package com.bgsoftware.superiorprison.plugin.test.script.function;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.Variable;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.getElseCreate;
import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.getVariable;

public class EconFunctions {
    public static final Pattern GET_BALANCE_PATTERN = Pattern.compile("get balance of (%([^ ]+)%|([0-9]+)V)");
    public static final Pattern TAKE_BALANCE_PATTERN = Pattern.compile("take ([0-9]+V|[0-9]+|-[0-9]+) from ([0-9]+)V balance");

    private static final VaultHook hook = SuperiorPrisonPlugin
            .getInstance().getHookController()
            .findHook(() -> VaultHook.class)
            .get();

    public static class GetBalance implements Function<Number> {
        private int varId;

        @Override
        public void initialize(String string, GlobalVariableMap variableMap) {
            Matcher matcher = GET_BALANCE_PATTERN.matcher(string);
            matcher.find();

            String varName = matcher.groupCount() == 3 ? matcher.group(3) : matcher.group(1);
            this.varId = VariableHelper.getVariableAsOfflinePlayer(
                    varName,
                    variableMap
            ).getId();
        }

        @Override
        public Class<Number> getType() {
            return Number.class;
        }

        @Override
        public boolean isCacheable() {
            return true;
        }

        @Override
        public Number execute(GlobalVariableMap globalVariables) {
            GlobalVariableMap.VariableData variableData = globalVariables.getVariableDataById(varId);

            Variable<?> variable = variableData.getVariable();
            if (!Player.class.isAssignableFrom(variableData.getVariable().getType()) && !Prisoner.class.isAssignableFrom(variableData.getVariable().getType()))
                throw new IllegalStateException("Failed to validate variable by id " + varId + " required type: Player or Prisoner. Found: " + variableData.getVariable().getType());

            Object o = variable.get(globalVariables);
            return hook.getBalance(o instanceof Prisoner ? ((Prisoner) o).getOfflinePlayer() : (OfflinePlayer) o).doubleValue();
        }

        @Override
        public String getId() {
            return "GetBalance";
        }
    }

    public static class TakeBalance implements Function<Boolean> {
        private int amountId;
        private int fromWhoId;

        @Override
        public void initialize(String string, GlobalVariableMap variableMap) {
            Matcher matcher = TAKE_BALANCE_PATTERN.matcher(string);
            matcher.find();

            String _amount = matcher.group(1);
            String _fromWho = matcher.group(2);

            // Initialize amount
            amountId = getElseCreate(_amount, variableMap).getId();
            fromWhoId = getVariable(_fromWho, variableMap).getId();
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
            GlobalVariableMap.VariableData _fromWho = globalVariables.getVariableDataById(fromWhoId);
            GlobalVariableMap.VariableData _amount = globalVariables.getVariableDataById(amountId);

            Object who = _fromWho.getVariable().get(globalVariables);
            Object amount = _amount.getVariable().get(globalVariables);

            Preconditions.checkArgument(who instanceof OfflinePlayer || who instanceof Prisoner, "Failed to find a valid player by variable id: " + fromWhoId);
            Preconditions.checkArgument(amount instanceof Number, "Failed to find valid number by id: " + amountId);

            hook.withdrawPlayer(who instanceof OfflinePlayer ? (OfflinePlayer) who : ((Prisoner) who).getOfflinePlayer(), BigDecimal.valueOf(((Number) amount).doubleValue()));
            return true;
        }

        @Override
        public String getId() {
            return "TakeBalance";
        }
    }
}
