package com.bgsoftware.superiorprison.plugin.economy;

import java.math.BigDecimal;

public interface Transaction {
    // Push changes
    void push();

    // Add amount to withdraw
    void add(BigDecimal decimal);
}
