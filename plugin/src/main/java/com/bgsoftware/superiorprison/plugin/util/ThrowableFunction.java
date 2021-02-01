package com.bgsoftware.superiorprison.plugin.util;

public interface ThrowableFunction<A, R> {
  R accept(A a) throws Throwable;
}
