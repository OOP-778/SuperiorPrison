package com.bgsoftware.superiorprison.api.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface NumberWrapper extends Comparable<NumberWrapper> {
  long toLong();

  double toDouble();

  int toInt();

  BigInteger toBigInt();

  BigDecimal toBigDecimal();

  boolean isInt();

  boolean isLong();

  boolean isDouble();

  boolean isBigInt();

  boolean isBigDecimal();

  Class<? extends Number> getType();

  /**
   * @param withSuffixes = should the number be formatted with suffixes or just with commas
   * @return a formatted number
   */
  String formatted(boolean withSuffixes);

  int compareTo(Number number);

  // Normalizes number and returns new object
  // Exmaple if we've got BigInt, but the lenght of it is equal to int
  // It will normalize it to int instead
  NumberWrapper normalize();
}
