package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackUpgrade;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.requirement.DeclinedRequirement;
import com.bgsoftware.superiorprison.plugin.requirement.RequirementHolder;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.menu.MenuAction;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.ArrayList;
import java.util.List;

public class BackPackUpgradeMenu extends OMenu implements BackpackLockable {
    private final SBackPack backPack;

    public BackPackUpgradeMenu(SPrisoner viewer, SBackPack backPack) {
        super("backpackupgrade", viewer);
        this.backPack = backPack;

        clickHandler("continue")
                .handle(event -> {
                    if (!backPack.getConfig().hasUpgrade()) return;

                    BackPackUpgrade<?> nextUpgrade = backPack.getConfig().getUpgrade(backPack.getCurrentLevel() + 1);
                    GlobalVariableMap variableMap = nextUpgrade.getVariableMap().clone();

                    variableMap.newOrReplace("prisoner", VariableHelper.createVariable(viewer));
                    OPair<Boolean, List<DeclinedRequirement>> meets = nextUpgrade.getRequirementHolder().meets(variableMap);

                    if (!meets.getSecond().isEmpty()) {
                        LocaleEnum.BACKPACK_UPGRADE_DONT_MEET_REQUIREMENTS.getWithErrorPrefix().send(getViewer().getPlayer());
                        return;
                    }

                    nextUpgrade.getRequirementHolder().take(variableMap);

                    backPack.upgrade(backPack.getCurrentLevel() + 1);
                    executeAction(MenuAction.RETURN);
                });

        getStateRequester()
                .registerRequest("info", this::request);
    }

    public OMenuButton.ButtonItemBuilder request(OMenuButton button) {
        if (backPack.getCurrentLevel() == backPack.getConfig().getMaxLevel()) {
            return button.getStateItem("max");

        } else
            return parseInfoButton(button.getStateItem("upgrade available"));
    }

    public OMenuButton.ButtonItemBuilder parseInfoButton(OMenuButton.ButtonItemBuilder item) {
        BackPackUpgrade<?> nextUpgrade = backPack.getConfig().getUpgrade(backPack.getCurrentLevel() + 1);

        GlobalVariableMap map = nextUpgrade.getVariableMap().clone();
        map.newOrReplace("prisoner", VariableHelper.createVariable(getViewer()));
        map.newOrReplace("backpack", VariableHelper.createVariable(backPack));

        List<String> newLore = new ArrayList<>();
        List<String> lore = item.itemBuilder().getLore();
        for (String s : lore) {
            if (s.contains("{backpack_nextlevel_description}")) {
                newLore.addAll(nextUpgrade.getDescription());

            } else if (s.contains("{REQUIREMENT}")) {
                String template = s.replace("{REQUIREMENT}", "");
                for (RequirementHolder.HoldingData requirementData : nextUpgrade.getRequirementHolder().getHoldingData()) {
                    String current = TextUtil.beautify(requirementData.getData().getGetter().execute(map));
                    String required = TextUtil.beautify(requirementData.getData().getCheckValue().execute(map));
                    String percentage = requirementData.getRequirement().getPercentage(requirementData.getData(), map) + "";
                    newLore.add(template.replace("{current}", current).replace("{needed}", required).replace("{percentage}", percentage).replace("{type}", requirementData.getData().getDisplayName()));
                }
            } else
                newLore.add(s.replace("{backpack_nextlevel}", nextUpgrade.getConfig().getLevel() + ""));
        }

        item.itemBuilder().setLore(newLore);
        return item;
    }
}
