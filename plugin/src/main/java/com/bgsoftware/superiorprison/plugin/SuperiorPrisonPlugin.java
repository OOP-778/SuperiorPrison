package com.bgsoftware.superiorprison.plugin;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.plugin.commands.CommandsRegister;
import com.bgsoftware.superiorprison.plugin.config.main.MainConfig;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.controller.BombController;
import com.bgsoftware.superiorprison.plugin.controller.ChatController;
import com.bgsoftware.superiorprison.plugin.controller.ConfigController;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.controller.HookController;
import com.bgsoftware.superiorprison.plugin.controller.PlaceholderController;
import com.bgsoftware.superiorprison.plugin.controller.PrestigeController;
import com.bgsoftware.superiorprison.plugin.controller.RankController;
import com.bgsoftware.superiorprison.plugin.controller.RequirementController;
import com.bgsoftware.superiorprison.plugin.controller.SBackPackController;
import com.bgsoftware.superiorprison.plugin.controller.SBlockController;
import com.bgsoftware.superiorprison.plugin.controller.STopController;
import com.bgsoftware.superiorprison.plugin.controller.TpsController;
import com.bgsoftware.superiorprison.plugin.data.LibLoader;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.bgsoftware.superiorprison.plugin.hook.impl.MVDWPapi;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.ShopGuiPlusHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.TokenEnchantHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.listeners.BackPackListener;
import com.bgsoftware.superiorprison.plugin.listeners.BombListener;
import com.bgsoftware.superiorprison.plugin.listeners.FlagsListener;
import com.bgsoftware.superiorprison.plugin.listeners.MineListener;
import com.bgsoftware.superiorprison.plugin.listeners.PrisonerListener;
import com.bgsoftware.superiorprison.plugin.listeners.RewardsListener;
import com.bgsoftware.superiorprison.plugin.listeners.StatisticsListener;
import com.bgsoftware.superiorprison.plugin.metrics.Metrics;
import com.bgsoftware.superiorprison.plugin.nms.SuperiorNms;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.requirement.RequirementRegisterer;
import com.bgsoftware.superiorprison.plugin.tasks.NewMineResetTask;
import com.bgsoftware.superiorprison.plugin.tasks.PlayerInventoryUpdateTask;
import com.bgsoftware.superiorprison.plugin.tasks.ResetQueueTask;
import com.bgsoftware.superiorprison.plugin.tasks.TasksStarter;
import com.bgsoftware.superiorprison.plugin.util.menu.MenuListener;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.h2.H2Dependencies;
import com.oop.datamodule.mongodb.MongoDependencies;
import com.oop.datamodule.mysql.MySqlDependencies;
import com.oop.datamodule.postgresql.PostgreSqlDependencies;
import com.oop.datamodule.sqlite.SqliteDependencies;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.StaticTask;
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

  @Setter private MainConfig mainConfig;
  private RequirementController requirementController;
  private PrestigeController prestigeController;
  private RankController rankController;
  private HookController hookController;
  private SBackPackController backPackController;
  private DatabaseController databaseController;
  private ChatController chatController;
  private SBlockController blockController;
  private STopController topController;
  private BombController bombController;
  private SuperiorNms nms;
  private PlayerInventoryUpdateTask inventoryUpdateTask;
  private TpsController tpsController;

  public static SuperiorPrisonPlugin getInstance() {
    return instance;
  }

  @Override
  public void enable() {
    instance = this;

    getOLogger().setMainColor("&d");
    getOLogger().setSecondaryColor("&5");

    try {
      // Setup NMS
      if (!setupNms()) {
        Bukkit.getPluginManager().disablePlugin(this);
        return;
      }

      StorageInitializer.initialize(
          StaticTask.getInstance()::async,
          StaticTask.getInstance()::sync,
          new LibLoader(this),
          null,
          error -> getOLogger().error(error),
          new MySqlDependencies(),
          new MongoDependencies(),
          new SqliteDependencies(),
          new PostgreSqlDependencies(),
          new H2Dependencies()
      );

      new MenuListener();

      this.hookController = new HookController();
      hookController.registerHooks(
          () -> VaultHook.class,
          () -> ShopGuiPlusHook.class,
          () -> PapiHook.class,
          () -> MVDWPapi.class,
          () -> TokenEnchantHook.class);

      // Setup API
      new SuperiorPrisonAPI(this);

      // Make sure plugin data folder exists
      if (!getDataFolder().exists()) getDataFolder().mkdirs();

      this.requirementController = new RequirementController();
      new RequirementRegisterer();

      inventoryUpdateTask = new PlayerInventoryUpdateTask();

      this.tpsController = new TpsController();
      this.configController = new ConfigController();
      this.chatController = new ChatController();
      this.backPackController = new SBackPackController();
      this.prestigeController = new PrestigeController();
      this.rankController = new RankController();
      this.bombController = new BombController();
      this.blockController = new SBlockController();

      getPluginComponentController()
          .add(configController, true)
          .add(rankController, true)
          .add(prestigeController, true)
          .add(chatController, true)
          .add(backPackController, true)
          .add(bombController, true)
          .load();

      this.databaseController = new DatabaseController();
      this.placeholderController = new PlaceholderController();
      this.topController = new STopController();

      // Initialize listeners
      new FlagsListener();
      new MineListener();
      new PrisonerListener();
      new StatisticsListener();
      new BackPackListener();
      new BombListener();
      new RewardsListener();

      // Initialize tasks
      new TasksStarter();

      // Register commands
      CommandsRegister.register();

//      Updater.setPlugin(this);
//      if (Updater.isOutdated()) {
//        getOLogger().printWarning("");
//        getOLogger().printWarning("A new version is available {}!", Updater.getLatestVersion());
//        getOLogger().printWarning("Version's Description: {}", Updater.getVersionDescription());
//        getOLogger().printWarning("");
//      }

      new Metrics(this);
      new NewMineResetTask();
      inventoryUpdateTask.execute();
    } catch (Throwable thrw) {
      Bukkit.getPluginManager().disablePlugin(this);
      throw new IllegalStateException("Failed to start SuperiorPrison", thrw);
    }
  }

  @Override
  public void disable() {
    if (getDatabaseController() != null) {
      getDatabaseController().save(false);
      getDatabaseController().shutdown();
    }

    getHookController().executeIfFound(() -> PapiHook.class, PapiHook::disable);

    Bukkit.getOnlinePlayers()
        .forEach(
            player -> {
              if (player.getInventory() instanceof PatchedInventory) {
                ((PatchedInventory) player.getInventory())
                    .getOwner()
                    .getBackPackMap()
                    .forEach(
                        (key, backpack) -> {
                          backpack.save();
                          player.getInventory().setItem(key, backpack.updateManually());
                        });
              }

              player.kickPlayer(Helper.color(LocaleEnum.PRISON_SHUTDOWN.getMessage().raw()[0]));
            });

    instance = null;
    SuperiorPrisonAPI.onDisable();

    if (databaseController != null) databaseController.getMineHolder().clear();
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

  @Override
  public SStatisticHolder getStatisticsController() {
    return databaseController.getStatisticHolder();
  }

  public boolean setupNms() {
    String version = getServer().getClass().getPackage().getName().split("\\.")[3];
    try {
      Object o =
          Class.forName("com.bgsoftware.superiorprison.plugin.nms.NmsHandler_" + version)
              .newInstance();
      this.nms = (SuperiorNms) o;
      getOLogger().print("Server version: " + version.replace("_", ".") + ". Fully compatible!");
      return true;
    } catch (ClassNotFoundException e) {
      getOLogger()
          .printError(
              "Unsupported version " + version + ". Failed to find NmsHandler, contact author!");
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return false;
  }
}
