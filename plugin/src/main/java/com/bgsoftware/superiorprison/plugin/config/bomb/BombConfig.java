package com.bgsoftware.superiorprison.plugin.config.bomb;

import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.sound.OSound;
import com.oop.orangeengine.sound.WrappedSound;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class BombConfig {

  private final ItemBuilder item;
  private final String name;
  private final int radius;
  private long cooldown = 0;
  private String explosionParticle;
  private int particleShownAt = -1;
  private String trailParticle;
  private WrappedSound sound;

  public BombConfig(ConfigSection section) {
    this.name = section.getKey();
    this.radius = section.getAs("radius");
    this.item = ItemBuilder.fromConfiguration(section.getSection("item").get());

    section.ifValuePresent("explosion particle", String.class, ep -> explosionParticle = ep);
    section.ifValuePresent("trail particle", String.class, tp -> trailParticle = tp);
    section.ifValuePresent("particle shown at", int.class, pst -> particleShownAt = pst);
    section.ifValuePresent("cooldown", String.class, cd -> cooldown = TimeUtil.toSeconds(cd));
    section.ifSectionPresent(
        "sound",
        soundSection -> {
          sound =
              WrappedSound.of(
                  OSound.match(soundSection.getAs("type")),
                  soundSection.getAs("pitch", float.class),
                  soundSection.getAs("volume", float.class));
        });
  }

  public ItemStack getItem() {
    return item.clone().addNBTTag("SP_BOMB", name).getItemStack();
  }
}
