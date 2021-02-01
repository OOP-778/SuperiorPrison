package com.bgsoftware.superiorprison.plugin.util.input.multi;

import java.util.Map;
import java.util.Optional;

public class MultiInputData {
  private final Map<String, Object> data;

  public MultiInputData(Map<String, Object> parsedData) {
    this.data = parsedData;
  }

  public <T> Optional<T> get(String arg) {
    return (Optional<T>) Optional.ofNullable(this.data.get(arg));
  }

  public <T> Optional<T> get(String arg, Class<T> type) {
    return (Optional<T>) Optional.ofNullable(this.data.get(arg));
  }

  public <T> T getAsReq(String arg) {
    return (T) this.data.get(arg);
  }

  public <T> T getAsReq(String arg, Class<T> type) {
    return type.cast(this.data.get(arg));
  }
}
