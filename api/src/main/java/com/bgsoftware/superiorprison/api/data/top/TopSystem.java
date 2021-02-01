package com.bgsoftware.superiorprison.api.data.top;

import java.util.List;

public interface TopSystem<T extends TopEntry> {
  String getName();

  List<T> getEntries();

  void update(int entriesLimit);
}
