package net.milkbowl.vault.economy.plugins;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.api.controller.EconomyController;
import com.bgsoftware.superiorprison.api.data.account.EconomyAccount;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class Economy_SuperiorPrison extends AbstractEconomy {
    private final boolean enabled;
    private final Pattern uuidPattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    public Economy_SuperiorPrison(Plugin vault) {
        Plugin spPlugin = Bukkit.getPluginManager().getPlugin("SuperiorPrison");

        if (!spPlugin.getDataFolder().exists())
            spPlugin.getDataFolder().mkdirs();

        File economyConfigFile = new File(spPlugin.getDataFolder(), "economy.yml");
        if (!economyConfigFile.exists())
            spPlugin.saveResource("economy.yml", false);

        YamlConfiguration economyConfig = YamlConfiguration.loadConfiguration(economyConfigFile);
        enabled = economyConfig.getBoolean("enabled");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return "Superior Prison";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public String format(double v) {
        return SuperiorPrisonAPI.getPlugin().getEconomyController().formatMoney(new BigDecimal(v));
    }

    @Override
    public String currencyNamePlural() {
        return "";
    }

    @Override
    public String currencyNameSingular() {
        return "";
    }

    @Override
    public boolean hasAccount(String s) {
        return true;
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return hasAccount(s);
    }

    @Override
    public double getBalance(String s) {
        EconomyController economyController = SuperiorPrisonAPI.getPlugin().getEconomyController();
        Optional<EconomyAccount> optAccount;
        if (uuidPattern.matcher(s).matches())
            optAccount = economyController.findAccountByUUID(UUID.fromString(s));
        else
            optAccount = economyController.findAccountByUsername(s);

        return optAccount.map(acc -> acc.getBalance().doubleValue()).orElse(0d);
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(s);
    }

    @Override
    public boolean has(String s, double v) {
        return getBalance(s) >= v;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return has(s, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        EconomyAccount account = SuperiorPrisonAPI.getPlugin().getEconomyController().getAccountByUsername(s);

        BigDecimal amount = BigDecimal.valueOf(v);
        account.remove(amount);

        return new EconomyResponse(v, account.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return withdrawPlayer(s, v);
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        EconomyAccount account = SuperiorPrisonAPI.getPlugin().getEconomyController().getAccountByUsername(s);

        BigDecimal amount = BigDecimal.valueOf(v);
        account.add(amount);

        return new EconomyResponse(v, account.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return depositPlayer(s, v);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public List<String> getBanks() {
        return new ArrayList<>();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return createPlayerAccount(s);
    }
}
