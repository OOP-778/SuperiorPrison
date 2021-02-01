package com.bgsoftware.superiorprison.plugin.commands.rankup;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.PermissionsInitializer;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.oop.orangeengine.command.OCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.bukkit.entity.Player;

public class CmdMaxRankup extends OCommand {
  public CmdMaxRankup() {
    label("maxrankup");
    ableToExecute(Player.class);
    onCommand(
        command -> {
          Player player = command.getSenderAsPlayer();
          SPrisoner prisoner =
              SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);
          SLadderRank oldRank = (SLadderRank) prisoner.getCurrentLadderRank();

          boolean changed = false;

          // Max Rankup
          while (prisoner.getCurrentLadderRank().getNext().isPresent()) {
            SLadderRank nextRank = (SLadderRank) prisoner.getCurrentLadderRank().getNext().get();

            List<RequirementException> failed =
                testRequirements(nextRank.getRequirements(), prisoner);
            if (!failed.isEmpty()) break;

            takeRequirements(nextRank.getRequirements(), prisoner);
            prisoner.setLadderRank(nextRank, true);
            changed = true;
          }

          // Max Prestige
          Optional<SPrestige> oldPrestige = prisoner.getCurrentPrestige().map(p -> (SPrestige) p);

          if (!prisoner.getCurrentLadderRank().getNext().isPresent()) {
            SPrestige next;
            if (!oldPrestige.isPresent())
              next =
                  (SPrestige)
                      SuperiorPrisonPlugin.getInstance()
                          .getPrestigeController()
                          .getPrestige(1)
                          .get();
            else next = (SPrestige) oldPrestige.get().getNext().orElse(null);

            while (next != null) {
              List<RequirementException> failed =
                  testRequirements(next.getRequirements(), prisoner);
              if (!failed.isEmpty()) break;

              takeRequirements(next.getRequirements(), prisoner);
              prisoner.setPrestige(next, true);
              changed = true;

              next = (SPrestige) next.getNext().orElse(null);
            }
          }

          if (changed) prisoner.save(true);

          // Send rank changes
          if (!oldRank.getName().contentEquals(prisoner.getCurrentLadderRank().getName()))
            messageBuilder(LocaleEnum.MAX_RANKUP_RANK_CHANGES.getWithPrefix())
                .replace("{starting_rank}", oldRank.getName())
                .replace("{current_rank}", prisoner.getCurrentLadderRank().getName())
                .send(command);

          // Send prestige changes
          if (!oldPrestige.isPresent() && prisoner.getCurrentPrestige().isPresent()
              || oldPrestige.isPresent()
                  && oldPrestige.get().getOrder()
                      < prisoner.getCurrentPrestige().get().getOrder()) {
            messageBuilder(LocaleEnum.MAX_RANKUP_PRESTIGE_CHANGES.getWithPrefix())
                .replace(
                    "{starting_prestige}",
                    oldPrestige.isPresent() ? oldPrestige.get().getName() : "N/A")
                .replace("{current_prestige}", prisoner.getCurrentPrestige().get().getName())
                .send(command);
          }
        });
    PermissionsInitializer.registerPrisonerCommand(this);
  }

  public List<RequirementException> testRequirements(
      Set<RequirementData> datas, SPrisoner prisoner) {
    List<RequirementException> failed = new ArrayList<>();
    datas.forEach(
        data -> {
          Optional<Requirement> requirement =
              SuperiorPrisonPlugin.getInstance()
                  .getRequirementController()
                  .findRequirement(data.getType());
          if (!requirement.isPresent()) return;
          try {
            requirement.get().getHandler().testIO(prisoner, data);
          } catch (RequirementException ex) {
            failed.add(ex);
          }
        });
    return failed;
  }

  public void takeRequirements(Set<RequirementData> datas, SPrisoner prisoner) {
    datas.forEach(
        data -> {
          Optional<Requirement> requirement =
              SuperiorPrisonPlugin.getInstance()
                  .getRequirementController()
                  .findRequirement(data.getType());
          if (!requirement.isPresent()) return;
          if (!data.isTake()) return;

          requirement.get().getHandler().take(prisoner, data);
        });
  }
}
