package com.bgsoftware.superiorprison.plugin.config.backpack;

import com.oop.orangeengine.yaml.ConfigSection;

public class SimpleBackPackConfig extends BackPackConfig<SimpleBackPackConfig> {
  static {
    registerUpgrade(
        "capacity",
        SimpleBackPackConfig.class,
        int.class,
        (back, i) -> {
          back.capacity = i;
        });
  }

  private int capacity;

  public SimpleBackPackConfig(ConfigSection section) {
    super(section);
  }

  public SimpleBackPackConfig() {}

  @Override
  public SimpleBackPackConfig clone() {
    SimpleBackPackConfig clone = new SimpleBackPackConfig();
    superClone(clone);
    clone.capacity = capacity;
    return clone;
  }

  @Override
  public int getCapacity() {
    return capacity;
  }
}
