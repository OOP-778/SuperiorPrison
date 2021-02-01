package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.top.TopSystem;

public interface TopController {
  <T extends TopSystem> T getSystem(Class<T> systemClass);

  void registerSystem(TopSystem system);
}
