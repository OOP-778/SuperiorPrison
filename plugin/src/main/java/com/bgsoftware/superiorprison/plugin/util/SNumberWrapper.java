package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.api.util.NumberWrapper;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.gson.internal.Primitives;
import com.oop.orangeengine.main.Engine;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class SNumberWrapper implements NumberWrapper {
  private Number number;

  private SNumberWrapper() {}

  public static SNumberWrapper of(Number number) {
    SNumberWrapper wrapper = new SNumberWrapper();
    wrapper.number = number;
    return wrapper;
  }

  public static Optional<SNumberWrapper> of(String input) {
    Number number = Helper.tryParse(input);
    if (number == null) return Optional.empty();

    SNumberWrapper wrapper = new SNumberWrapper();
    wrapper.number = number;
    return Optional.of(wrapper);
  }

  @Override
  public long toLong() {
    return number.longValue();
  }

  @Override
  public double toDouble() {
    return number.doubleValue();
  }

  @Override
  public int toInt() {
    return number.intValue();
  }

  public SNumberWrapper normalize() {
    return SNumberWrapper.of(Helper.normalize(number));
  }

  @Override
  public BigInteger toBigInt() {
    if (number instanceof BigInteger) return (BigInteger) number;
    return Helper.convertToBigDecimal(number).toBigInteger();
  }

  @Override
  public BigDecimal toBigDecimal() {
    return Helper.convertToBigDecimal(number);
  }

  @Override
  public boolean isInt() {
    return Primitives.unwrap(number.getClass()) == int.class;
  }

  @Override
  public boolean isLong() {
    return Primitives.unwrap(number.getClass()) == long.class;
  }

  @Override
  public boolean isDouble() {
    return Primitives.unwrap(number.getClass()) == double.class;
  }

  @Override
  public boolean isBigInt() {
    return number instanceof BigInteger;
  }

  @Override
  public boolean isBigDecimal() {
    return number instanceof BigDecimal;
  }

  @Override
  public Class<? extends Number> getType() {
    return number.getClass();
  }

  @Override
  public String formatted(boolean withSuffixes) {
    return Helper.format(withSuffixes, number);
  }

  @Override
  public int compareTo(Number number) {
    return toBigDecimal().compareTo(Helper.convertToBigDecimal(number));
  }

  @Override
  public int compareTo(NumberWrapper o) {
    return o.toBigDecimal().compareTo(toBigDecimal());
  }

  private static class Helper {
    // Parsers from smallest number to biggest
    private static final Function<String, Number>[] non_decimal_parsers =
        new Function[] {
          in -> Ints.tryParse(String.valueOf(in)),
          in -> Longs.tryParse(String.valueOf(in)),
          in -> new BigInteger(String.valueOf(in))
        };

    // Decimal parsers from smallest number to biggest
    private static final Function<String, Number>[] decimal_parsers =
        new Function[] {
          in -> Floats.tryParse(String.valueOf(in)),
          in -> Doubles.tryParse(String.valueOf(in)),
          in -> new BigDecimal(String.valueOf(in))
        };

    private static final Map<Integer, Function<String, Number>> non_decimal_normalizers =
        new HashMap<>();
    private static final Map<Integer, Function<String, Number>> decimal_normalizers =
        new HashMap<>();
    private static final DecimalFormat format_without_commas = new DecimalFormat("###.##");
    private static final NumberFormat current_format = NumberFormat.getCurrencyInstance();
    private static final BigDecimal BIG_DECIMAL_THOUSAND = BigDecimal.valueOf(1000);

    static {
      non_decimal_normalizers.put(10, Ints::tryParse);
      non_decimal_normalizers.put(19, Longs::tryParse);

      decimal_normalizers.put(17, Doubles::tryParse);
    }

    private static BigDecimal convertToBigDecimal(Number number) {
      if (number instanceof BigDecimal) return (BigDecimal) number;

      if (number instanceof BigInteger) return new BigDecimal(((BigInteger) number));

      if (number instanceof Double) return BigDecimal.valueOf(number.doubleValue());

      if (number instanceof Integer) return new BigDecimal(number.intValue());

      return new BigDecimal(number.toString());
    }

    public static String format(boolean suffixes, Number passed) {
      if (!suffixes) return current_format.format(passed);

      int suffixIndex = 0;
      BigDecimal decimal = convertToBigDecimal(passed);

      String s = decimal.toPlainString();
      while (s.length() > 3) {
        s = s.substring(3);
        suffixIndex++;
      }

      decimal = decimal.divide(BIG_DECIMAL_THOUSAND.pow(suffixIndex));

      // To round it to 2 digits.
      decimal = decimal.setScale(2, BigDecimal.ROUND_FLOOR);

      // Add the number with the denomination to get the final value.
      return decimal.stripTrailingZeros().toPlainString() + getSuffixAt(suffixIndex);
    }

    private static String getSuffixAt(int index) {
      try {
        return SuperiorPrisonPlugin.getInstance()
            .getMainConfig()
            .getNumberFormatterSuffixes()
            .get(index);
      } catch (Throwable throwable) {
        Engine.getEngine().getLogger().printWarning("Invalid suffix at index {}", index);
      }

      return "-N";
    }

    public static Number tryParse(String input) {
      try {
        // If "string" contains . parse as decimal
        if (StringUtils.contains(".", input)) return useParsers(decimal_parsers, input);
        else return useParsers(non_decimal_parsers, input);

      } catch (Throwable throwable) {
        return null;
      }
    }

    private static Number useParsers(Function<String, Number>[] parsers, String what) {
      for (Function<String, Number> parser : parsers) {
        Number apply = parser.apply(what);
        if (apply != null) return apply;
      }

      return null;
    }

    public static Number normalize(Number number) {
      String format = format_without_commas.format(number);
      int length = format.length();

      if (!format.contains(".")) {
        for (Map.Entry<Integer, Function<String, Number>> normalizerEntry :
            non_decimal_normalizers.entrySet()) {
          if (normalizerEntry.getKey() <= length) {
            try {
              Number parsed = normalizerEntry.getValue().apply(format);
              if (parsed != null) return parsed;
            } catch (Throwable throwable) {
              // In case it fails to parse as seen in some situations,
              // We just skip this normalizer
            }
          }
        }

        // If none succeed to normalize we return BigInt
        return new BigInteger(format);
      }

      length = StringUtils.split(format, ".")[0].length();
      for (Map.Entry<Integer, Function<String, Number>> normalizerEntry :
          decimal_normalizers.entrySet()) {
        if (normalizerEntry.getKey() <= length) {
          try {
            Number parsed = normalizerEntry.getValue().apply(format);
            if (parsed != null) return parsed;
          } catch (Throwable throwable) {
            // In case it fails to parse as seen in some situations,
            // We just skip this normalizer
          }
        }
      }

      return new BigDecimal(format);
    }
  }
}
