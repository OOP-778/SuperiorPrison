package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.TopController;
import com.bgsoftware.superiorprison.api.data.top.TopSystem;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.main.TopSystemsSection;
import com.bgsoftware.superiorprison.plugin.object.top.balance.SBalanceTopSystem;
import com.bgsoftware.superiorprison.plugin.object.top.blocks.SBlocksTopSystem;
import com.bgsoftware.superiorprison.plugin.object.top.prestige.SPrestigeTopSystem;
import com.oop.orangeengine.main.task.OTask;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class STopController implements TopController {
    private final Set<TopSystem> registeredSystems = new HashSet<>();
    private final Map<String, Long> updateTimes = new HashMap<>();
    private final Map<String, ZonedDateTime> timesWhenRan = new HashMap<>();

    public STopController() {
        registerSystem(new SBlocksTopSystem());
        registerSystem(new SPrestigeTopSystem());
        registerSystem(new SBalanceTopSystem());

        new OTask()
                .delay(TimeUnit.SECONDS, 1)
                .repeat(true)
                .sync(false)
                .runnable(this::update)
                .execute();
    }

    @Override
    public <T extends TopSystem> T getSystem(Class<T> systemClass) {
        return (T) registeredSystems
                .stream()
                .filter(ts -> systemClass.isAssignableFrom(ts.getClass()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void registerSystem(TopSystem system) {
        registeredSystems.add(system);
    }

    public void update() {
        TopSystemsSection config = SuperiorPrisonPlugin.getInstance().getMainConfig().getTopSystemsSection();
        for (TopSystem registeredSystem : registeredSystems) {
            Long delay = updateTimes.get(registeredSystem.getName());

            if (delay != null) {
                delay--;
                updateTimes.remove(registeredSystem.getName());
                updateTimes.put(registeredSystem.getName(), delay);
                if (delay != 0) continue;
            }

            timesWhenRan.remove(registeredSystem.getName());
            timesWhenRan.put(registeredSystem.getName(), ZonedDateTime.now(ZoneId.systemDefault()));

            registeredSystem.update(
                    Optional.ofNullable(config.getConfig(registeredSystem.getName())).map(TopSystemsSection.TopSystemConfig::getLimit).orElse(10)
            );

            updateTimes.remove(registeredSystem.getName());
            long defaultInterval = 10;
            updateTimes.put(
                    registeredSystem.getName(),
                    Optional.ofNullable(config.getConfig(registeredSystem.getName())).map(TopSystemsSection.TopSystemConfig::getInterval).orElse(defaultInterval)
            );
        }
    }
}
