package com.bgsoftware.superiorprison.api.requirement;

public interface DeclinedRequirement {
    String getDisplayName();
    Object getCurrent();
    Object getRequired();
}
