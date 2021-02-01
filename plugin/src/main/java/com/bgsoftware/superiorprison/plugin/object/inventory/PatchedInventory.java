package com.bgsoftware.superiorprison.plugin.object.inventory;

public interface PatchedInventory {
  void setOwnerCalling();

  boolean isOwnerCalling();

  SPlayerInventory getOwner();
}
