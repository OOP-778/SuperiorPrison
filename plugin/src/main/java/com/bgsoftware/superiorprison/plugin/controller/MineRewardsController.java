package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.minerewards.MineReward;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Maps;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.yaml.OConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public class MineRewardsController implements OComponent<SuperiorPrisonPlugin> {

    private Map<Integer, MineReward> rewardMap = Maps.newHashMap();

    public int getProcentageCompleted(int rewardPlace, Player player) {
        MineReward mineReward = rewardMap.get(rewardPlace);
        if (mineReward == null) return -1;

        Optional<Prisoner> prisonerOptional = SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(player.getUniqueId());
        if (!prisonerOptional.isPresent()) return -1;

        SPrisoner prisoner = (SPrisoner) prisonerOptional.get();

        long completed = 0;
        long required = 0;
        for (OPair<OMaterial, Long> requirement : mineReward.getRequirements()) {
            required += requirement.getSecond();
            completed += prisoner.getMinedBlocks().getOrDefault(requirement.getFirst().parseMaterial(), 0L);
        }

        return (int) (completed / required) * 100;
    }

    public int getCurrentRewardPlace(Player player) {
        Optional<Prisoner> prisonerOptional = SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(player.getUniqueId());
        if (!prisonerOptional.isPresent()) return 1;

        SPrisoner prisoner = (SPrisoner) prisonerOptional.get();
        if (prisoner.getCompletedMineRewards().isEmpty()) return 1;

        return prisoner.getCompletedMineRewards()
                .stream()
                .skip(prisoner.getCompletedMineRewards().size() - 1)
                .findFirst().get() + 1;
    }

    @Override
    public boolean load() {
        try {
            rewardMap.clear();

            OConfiguration config = SuperiorPrisonPlugin.getInstance().getConfigController().getMinesRewardsConfig();
            config.getSections().values().forEach(section -> rewardMap.put(Integer.valueOf(section.getKey()), new MineReward(section)));


        } catch (Throwable thrw) {
            getPlugin().getOLogger().error(thrw);
            return false;
        }

        return true;
    }
}
