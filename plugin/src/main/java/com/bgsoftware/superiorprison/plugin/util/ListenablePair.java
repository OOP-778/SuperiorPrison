package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Setter;
import lombok.experimental.Accessors;

public class ListenablePair<F, S> extends OPair<F, S> {

  @Setter
  @Accessors(chain = true, fluent = true)
  private Runnable onComplete;

  public ListenablePair(F first, S second) {
    super(first, second);
  }

  public void complete() {
    if (onComplete != null) onComplete.run();
  }
}
