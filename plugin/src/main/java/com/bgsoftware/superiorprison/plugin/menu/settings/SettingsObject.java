package com.bgsoftware.superiorprison.plugin.menu.settings;

import com.bgsoftware.superiorprison.plugin.util.ThrowableFunction;
import com.oop.orangeengine.message.OMessage;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@Getter
@Setter
public class SettingsObject<T> {

  private @NonNull ThrowableFunction<String, T> mapper;
  private OMessage requestMessage;
  private OMessage completeMessage;
  private @NonNull Consumer<T> onComplete;
  private @NonNull String id;
  private T currentValue;
  private Class<T> type;

  public SettingsObject(Class<T> valueType, T currentValue) {
    this.currentValue = currentValue;
    this.type = valueType;
  }
}
