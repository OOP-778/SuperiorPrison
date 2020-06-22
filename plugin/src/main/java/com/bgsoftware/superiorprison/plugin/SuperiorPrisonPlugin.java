package com.bgsoftware.superiorprison.plugin;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.plugin.commands.CommandsRegister;
import com.bgsoftware.superiorprison.plugin.config.MainConfig;
import com.bgsoftware.superiorprison.plugin.controller.*;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.ShopGuiPlusHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.listeners.FlagsListener;
import com.bgsoftware.superiorprison.plugin.listeners.MineListener;
import com.bgsoftware.superiorprison.plugin.listeners.PrisonerListener;
import com.bgsoftware.superiorprison.plugin.listeners.StatisticsListener;
import com.bgsoftware.superiorprison.plugin.nms.SuperiorNms;
import com.bgsoftware.superiorprison.plugin.requirement.RequirementRegisterer;
import com.bgsoftware.superiorprison.plugin.tasks.TasksStarter;
import com.bgsoftware.superiorprison.plugin.util.menu.MenuListener;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.TaskController;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

@Getter
public class SuperiorPrisonPlugin extends EnginePlugin implements SuperiorPrison {

    public static boolean disabling = false;
    private static SuperiorPrisonPlugin instance;
    private PlaceholderController placeholderController;
    private ConfigController configController;

    @Setter
    private MainConfig mainConfig;
    private RequirementController requirementController;
    private PrestigeController prestigeController;
    private RankController rankController;
    private HookController hookController;
    private DatabaseController databaseController;
    private SStatisticHolder statisticsController;
    private ChatController chatController;
    private SuperiorNms nms;

    public static SuperiorPrisonPlugin getInstance() {
        return instance;
    }

    @Override
    public void enable() {
        instance = this;

        try {
            // Setup NMS
            if (!setupNms()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            new MenuListener();
            this.configController = new ConfigController();
            getPluginComponentController()
                    .add(configController, true)
                    .load();

            this.hookController = new HookController();
            hookController.registerHooks(() -> VaultHook.class, () -> ShopGuiPlusHook.class, () -> PapiHook.class);

            // Setup API
            new SuperiorPrisonAPI(this);

            // Make sure plugin data folder exists
            if (!getDataFolder().exists())
                getDataFolder().mkdirs();

            this.databaseController = new DatabaseController(mainConfig);
            this.statisticsController = databaseController.getStatisticHolder();
            this.prestigeController = new PrestigeController(true);
            this.rankController = new RankController(true);
            this.chatController = new ChatController();
            chatController.load();

            getPluginComponentController()
                    .add(rankController, true)
                    .add(prestigeController, true)
                    .add(chatController, true);

            this.placeholderController = new PlaceholderController();
            this.requirementController = new RequirementController();
            new RequirementRegisterer();

            // Initialize listeners
            new FlagsListener();
            new MineListener();
            new PrisonerListener();
            new StatisticsListener();

            // Initialize tasks
            new TasksStarter();

            // Register commands
            CommandsRegister.register();
        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to start SuperiorPrison", thrw);
        }
    }

    @Override
    public void disable() {
        if (getDatabaseController() != null && getDatabaseController().getDatabase() != null)
            getDatabaseController().save(false);

        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(Helper.color("&cPrison shutting down...")));
        instance = null;
    }

    @Override
    public TaskController provideTaskController() {
        return new ClassicTaskController(this);
    }

    @Override
    public SMineHolder getMineController() {
        return databaseController.getMineHolder();
    }

    @Override
    public SPrisonerHolder getPrisonerController() {
        return databaseController.getPrisonerHolder();
    }

    public boolean setupNms() {
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            Object o = Class.forName("com.bgsoftware.superiorprison.plugin.nms.NmsHandler_" + version).newInstance();
            this.nms = (SuperiorNms) o;
            getOLogger().print("Server version: " + version.replace("_", " .") + ". Fully compatible!");
            return true;
        } catch (ClassNotFoundException e) {
            getOLogger().printError("Unsupported version " + version + ". Failed to find NmsHandler, contact author!");
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}