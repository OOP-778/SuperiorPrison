package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackUpgrade;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.menu.MenuAction;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackPackUpgradeMenu extends OMenu implements BackpackLockable {

  private final SBackPack backPack;

  public BackPackUpgradeMenu(SPrisoner viewer, SBackPack backPack) {
    super("backpackupgrade", viewer);
    this.backPack = backPack;

    clickHandler("continue")
        .handle(
            event -> {
              if (!backPack.getConfig().hasUpgrade()) return;

              BackPackUpgrade nextUpgrade =
                  backPack.getConfig().getUpgrade(backPack.getCurrentLevel() + 1);
              List<RequirementException> failed = new ArrayList<>();
              nextUpgrade
                  .getRequirements()
                  .forEach(
                      data -> {
                        Optional<Requirement> requirement =
                            SuperiorPrisonPlugin.getInstance()
                                .getRequirementController()
                                .findRequirement(data.getType());
                        if (!requirement.isPresent()) return;

                        try {
                          requirement.get().getHandler().testIO(getViewer(), data);
                        } catch (RequirementException ex) {
                          failed.add(ex);
                        }
                      });

              if (!failed.isEmpty()) {
                LocaleEnum.BACKPACK_UPGRADE_DONT_MEET_REQUIREMENTS
                    .getWithErrorPrefix()
                    .send(getViewer().getPlayer());
                return;
              }

              nextUpgrade
                  .getRequirements()
                  .forEach(
                      data -> {
                        Optional<Requirement> requirement =
                            SuperiorPrisonPlugin.getInstance()
                                .getRequirementController()
                                .findRequirement(data.getType());
                        if (!requirement.isPresent()) return;
                        if (!data.isTake()) return;

                        requirement.get().getHandler().take(getViewer(), data);
                      });

              backPack.upgrade(backPack.getCurrentLevel() + 1);
              executeAction(MenuAction.REFRESH);
            });

    getStateRequester().registerRequest("info", this::request);
  }

  public OMenuButton.ButtonItemBuilder request(OMenuButton button) {
    if (backPack.getCurrentLevel() == backPack.getConfig().getMaxLevel()) {
      OMenuButton.ButtonItemBuilder max = button.getStateItem("max");
      return max;

    } else return parseInfoButton(button.getStateItem("upgrade available"));
  }

  public OMenuButton.ButtonItemBuilder parseInfoButton(OMenuButton.ButtonItemBuilder item) {
    BackPackUpgrade nextUpgrade =
        backPack.getConfig().getUpgrade(backPack.getCurrentLevel() + 1);

    List<String> newLore = new ArrayList<>();
    List<String> lore = item.itemBuilder().getLore();
    for (String s : lore) {
      if (s.contains("{backpack_nextlevel_description}")) {
        newLore.addAll(nextUpgrade.getDescription());

      } else if (s.contains("{REQUIREMENT}")) {
        String template = s.replace("{REQUIREMENT}", "");
        for (RequirementData requirementData : nextUpgrade.getRequirements()) {
          Optional<Requirement> optionalRequirement =
              SuperiorPrisonPlugin.getInstance()
                  .getRequirementController()
                  .findRequirement(requirementData.getType());
          if (!optionalRequirement.isPresent()) continue;

          Requirement requirement = optionalRequirement.get();
          String current =
              TextUtil.beautifyNumber(
                  requirement.getHandler().getCurrent(getViewer(), requirementData));
          String required = TextUtil.beautifyNumber(requirementData.getValue());
          String percentage =
              requirement.getHandler().getPercentage(getViewer(), requirementData) + "";
          newLore.add(
              template
                  .replace("{current}", current)
                  .replace("{needed}", required)
                  .replace("{percentage}", percentage)
                  .replace("{type}", requirement.getId().toLowerCase()));
        }
      } else
        newLore.add(s.replace("{backpack_nextlevel}", nextUpgrade.getConfig().getLevel() + ""));
    }

    item.itemBuilder().setLore(newLore);
    return item;
  }
}
