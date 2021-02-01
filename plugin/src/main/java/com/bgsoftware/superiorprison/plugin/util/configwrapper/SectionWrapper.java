package com.bgsoftware.superiorprison.plugin.util.configwrapper;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.ConfigSection;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class SectionWrapper implements DefaultValues {

  @Getter private final Map<String, OPair<Object, String[]>> defaultValues = new HashMap<>();

  @Setter(AccessLevel.PROTECTED)
  @Getter
  private ConfigSection section;

  protected void initialize() {
    _init(section);
  }
}
