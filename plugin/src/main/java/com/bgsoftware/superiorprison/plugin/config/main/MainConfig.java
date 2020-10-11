package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.bgsoftware.superiorprison.plugin.util.configwrapper.ConfigWrapper;
import com.google.common.collect.Lists;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.yaml.Config;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class MainConfig extends ConfigWrapper {
    public Config configuration;
    private String locale = "en-us";

    private boolean shopGuiAsFallBack = false;

    private long cacheTime = TimeUnit.HOURS.toMillis(1);
    private long soldMessageInterval = TimeUnit.MINUTES.toMillis(3);
    private DatabaseSection database;
    private MineDefaultsSection mineDefaults;
    private OItem areaSelectionTool;

    private long rankupMessageInterval;
    private boolean resetRanks = false;

    private boolean useMineShopsByRank = false;
    private ProgressionScaleSection scaleSection;

    private PrisonerDefaults prisonerDefaults;
    private TopSystemsSection topSystemsSection;
    private PlaceholdersSection placeholdersSection;

    private List<OMaterial> disabledInteractableBlocks = new ArrayList<>();

    private int chunksPerTick;
    private long updateBackpacksEvery;
    private boolean itemDropping = false;

    private boolean handleNamedItems;

    public MainConfig() {
        load();
    }

    private void load() {
        addDefault("blocks cache time limit", "1h");
        this.configuration = new Config(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "config.yml").createIfNotExists(true));
        configuration.getSections().keySet().forEach(System.out::println);


        setConfig(configuration);

        // Set Locale
        configuration.ifValuePresent("locale", String.class, locale -> this.locale = locale);

        // Load Database Section
        this.database = addSection("database", new DatabaseSection());

        // Load prisoner defaults
        this.prisonerDefaults = addSection("prisoner defaults", new PrisonerDefaults());
        this.topSystemsSection = addSection("top systems", new TopSystemsSection());
        this.placeholdersSection = addSection("placeholders", new PlaceholdersSection());

        // Load Mine Defaults
        this.mineDefaults = addSection("mine defaults", new MineDefaultsSection());
        this.areaSelectionTool = new OItem().load(configuration.getSection("area selection tool").get());

        configuration.ifValuePresent("shopgui fall back", boolean.class, b -> shopGuiAsFallBack = b);

        cacheTime = TimeUtil.toSeconds(configuration.getAs("blocks cache time limit", String.class, () -> "1h", "How long will the blocks statistic cache blocks"));
        soldMessageInterval = TimeUtil.toSeconds(configuration.getAs("sold message interval", String.class, () -> "3m", "Sold blocks message interval"));
        rankupMessageInterval = TimeUtil.toSeconds(configuration.getAs("rankup message interval", String.class, () -> "6s", "How often it should check the rankup"));
        resetRanks = configuration.getAs("reset ranks after prestige up", boolean.class, () -> false, "Should it reset the ranks after prestige");
        useMineShopsByRank = configuration.getAs("use mine shops by rank", boolean.class, () -> false, "Should it use mine shop of the current rank of the player");
        chunksPerTick = configuration.getAs(
                "chunks per tick", int.class, () -> 4,
                "How much chunks per tick should the block setting use",
                "Please be careful with this. As it can cause serious performance issues, test the values you set before using."
        );

        disabledInteractableBlocks = (List<OMaterial>) configuration
                .getAs("disabled interactable blocks", List.class, () -> Lists.newArrayList("CRAFTING_TABLE", "ANVIL", "CHEST", "ITEM_FRAME"), "Disable interactable blocks")
                .stream()
                .map(ob -> OMaterial.matchMaterial(ob.toString()))
                .collect(Collectors.toList());

        scaleSection = new ProgressionScaleSection(configuration.getSection("progression scale").get());
        itemDropping = configuration.getAs("item dropping", boolean.class, () -> true, "Disable or Enable dropping items");
        updateBackpacksEvery = TimeUnit.SECONDS
                .toMillis(TimeUtil.toSeconds(configuration.getAs("update backpacks every", String.class, () -> "1s", "Update backpacks every")));

        handleNamedItems = configuration.getAs("handle named items", boolean.class, () -> false, "Handle items with name, lore, etc.", "In shops, auto sell, etc.");

        SuperiorPrisonPlugin.getInstance().getOLogger().setDebugMode(configuration.getAs("debug", boolean.class, () -> false));
        SuperiorPrisonPlugin.getInstance().getResetQueueTask().setChunksPerTick(chunksPerTick);
        SuperiorPrisonPlugin.getInstance().getInventoryUpdateTask().setUpdateEvery(updateBackpacksEvery);
        initialize();

        configuration.save();
    }
}
