package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetType;
import com.bgsoftware.superiorprison.plugin.util.configwrapper.SectionWrapper;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.yaml.ConfigSection;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class MineDefaultsSection extends SectionWrapper {

  private final OPair<ResetType, String> resetting = new OPair<>(ResetType.PERCENTAGE, "50");
  private OItem icon;
  private int limit = -1;
  private List<OPair<Double, OMaterial>> materials;

  private List<OPair<OMaterial, BigDecimal>> shopPrices;

  private boolean teleporation = false;
  private boolean disableEnderPearls = false;

  private boolean disableMonsterSpawn = false;
  private boolean disableAnimalSpawn = false;

  @Override
  protected void initialize() {
    ConfigSection section = getSection();
    this.icon = new OItem().load(section.getSection("icon").get());
    this.limit = section.getAs("limit");

    section.ifValuePresent("teleporation", boolean.class, bool -> this.teleporation = bool);
    section.ifValuePresent(
        "disable enderpearls", boolean.class, bool -> this.disableEnderPearls = bool);
    section.ifValuePresent(
        "disable monster spawn", boolean.class, bool -> this.disableMonsterSpawn = bool);
    section.ifValuePresent(
        "disable animal spawn", boolean.class, bool -> this.disableAnimalSpawn = bool);

    ConfigSection resettingSection = section.getSection("resetting").get();
    this.resetting.set(
        ResetType.valueOf(resettingSection.getAs("mode", String.class).toUpperCase()),
        resettingSection.getAs("value"));

    this.materials =
        ((List<String>) section.getAs("materials"))
            .stream()
                .map(string -> string.split(":"))
                .map(
                    array ->
                        new OPair<>(
                            Double.parseDouble(array[1]), OMaterial.matchMaterial(array[0])))
                .collect(Collectors.toList());

    this.shopPrices =
        !section.isValuePresent("shop items")
            ? new ArrayList<>()
            : ((List<String>) section.getAs("shop items"))
                .stream()
                    .map(string -> string.split(":"))
                    .map(
                        array ->
                            new OPair<>(
                                OMaterial.matchMaterial(array[0].toUpperCase()),
                                new BigDecimal(array[1])))
                    .collect(Collectors.toList());
  }
}
