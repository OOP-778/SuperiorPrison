package com.bgsoftware.superiorprison.plugin;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.plugin.commands.CommandsRegister;
import com.bgsoftware.superiorprison.plugin.config.main.MainConfig;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.controller.*;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.ShopGuiPlusHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.listeners.FlagsListener;
import com.bgsoftware.superiorprison.plugin.listeners.MineListener;
import com.bgsoftware.superiorprison.plugin.nms.ISuperiorNms;
import com.bgsoftware.superiorprison.plugin.requirement.RequirementRegisterer;
import com.bgsoftware.superiorprison.plugin.tasks.MineShowTask;
import com.bgsoftware.superiorprison.plugin.util.menu.MenuListener;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.ITaskController;
import com.oop.orangeengine.message.locale.Locale;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class SuperiorPrisonPlugin extends EnginePlugin implements SuperiorPrison {

    private static SuperiorPrisonPlugin instance;
    private PlaceholderController placeholderController;
    private ConfigController configController;
    private MainConfig mainConfig;
    private RequirementController requirementController;
    private RankController rankController;
    private HookController hookController;
    private DataController dataController;
    private ODatabase dab;
    private ISuperiorNms nms;

    public SuperiorPrisonPlugin() {
        instance = this;
    }

    public static SuperiorPrisonPlugin getInstance() {
        return instance;
    }

    @Override
    public void enable() {
        getOLogger().setDebugMode(true);

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
        hookController.registerHooks(VaultHook.class, PapiHook.class, ShopGuiPlusHook.class);

        // Setup API
        new SuperiorPrisonAPI(this);

        // Make sure plugin data folder exists
        if (!getDataFolder().exists())
            getDataFolder().mkdirs();

        this.mainConfig = new MainConfig();

        // Setup Database
        dab = mainConfig.getDatabase().getDatabase();

        // Initialize controllers
        this.rankController = new RankController(true);
        this.dataController = new DataController(dab);
        this.placeholderController = new PlaceholderController();
        this.requirementController = new RequirementController();
        new RequirementRegisterer();

        // Load locale
        Locale.load(mainConfig.getLocale());
        LocaleEnum.load();

        // Initialize listeners
        new FlagsListener();
        new MineListener();

        // Initialize tasks
        new MineShowTask();

        CommandController commandController = new CommandController(this);
        CommandsRegister.register(commandController);
    }

    @Override
    public void disable() {
        long then = System.currentTimeMillis();
        getDataController().saveAll();
        getOLogger().print("Save done! Took " + (System.currentTimeMillis() - then) + "ms");

        instance = null;
    }

    @Override
    public ITaskController provideTaskController() {
        return new ClassicTaskController(this);
    }

    @Override
    public DataController getMineController() {
        return dataController;
    }

    @Override
    public DataController getPrisonerController() {
        return dataController;
    }

    public boolean setupNms() {
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            Object o = Class.forName("com.bgsoftware.superiorprison.plugin.nms.NmsHandler_" + version).newInstance();
            this.nms = (ISuperiorNms) o;
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
