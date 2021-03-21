package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.util.DataUtil;
import com.oop.datamodule.lib.google.gson.JsonArray;
import com.oop.datamodule.lib.google.gson.JsonElement;
import com.oop.datamodule.lib.google.gson.JsonObject;
import com.oop.datamodule.lib.google.gson.JsonPrimitive;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.bgsoftware.superiorprison.plugin.util.ItemStackUtil.isNamed;

@Getter
public class BackPackData implements SerializableObject {

  private final Map<BackPackItem, BigInteger> items = new ConcurrentHashMap<>();
  private @NonNull final SBackPack holder;
  @Setter private int level;
  @Setter private @NonNull String configId;
  @Setter private boolean sell = false;

  public BackPackData(SBackPack backPack) {
    this.holder = backPack;

    try {
      if (holder.getConfig() != null) {
        this.level = holder.getConfig().getLevel();
        this.configId = holder.getConfig().getId();
      }
    } catch (Throwable ignored) {
    }
  }

  private static JsonElement wrap(ItemStack itemStack) {
    if (isNamed(itemStack)) return DataUtil.wrap(itemStack);
    else return new JsonPrimitive(OMaterial.matchMaterial(itemStack).name());
  }

  private static ItemStack unwrap(JsonElement element) {
    String unparsedString = element.toString();
    if (!unparsedString.contains("id")) {
      if (unparsedString.startsWith("\"")) unparsedString = unparsedString.substring(1);
      if (unparsedString.endsWith("\""))
        unparsedString = unparsedString.substring(0, unparsedString.length() - 1);

      String[] split = unparsedString.split("-");
      if (split.length == 1) return OMaterial.matchMaterial(split[0]).parseItem();
      else return OMaterial.matchMaterial(split[0]).parseItem(Integer.parseInt(split[1]));
    } else return DataUtil.fromElement(element, ItemStack.class);
  }

  @Override
  public void serialize(SerializedData serializedData) {
    // Level
    serializedData.write("level", level);

    // Config id
    serializedData.write("configId", configId);

    // Is sell
    serializedData.write("sell", sell);

    JsonArray array = new JsonArray();

    // Items
    for (Map.Entry<BackPackItem, BigInteger> itemEntry : items.entrySet()) {
      JsonObject object = new JsonObject();
      object.add("item", wrap(itemEntry.getKey().getItemStack()));
      object.addProperty("amount", itemEntry.getValue());
      array.add(object);
    }

    serializedData.write("items2", array);
  }

  @Override
  public void deserialize(SerializedData serializedData) {
    this.level = serializedData.applyAs("level", int.class);
    this.configId = serializedData.applyAs("configId", String.class);
    this.sell = serializedData.applyAs("sell", boolean.class, () -> false);

    // Old Method Big Chungus
    if (serializedData.has("items")) {
      for (JsonElement element :
          serializedData.getJsonElement().getAsJsonObject().getAsJsonArray("items")) {
        JsonArray itemArray = element.getAsJsonArray();
        ItemStack unwrap = unwrap(itemArray.get(1));
        if (unwrap == null) continue;

        items.merge(
            BackPackItem.wrap(unwrap), BigInteger.valueOf(unwrap.getAmount()), BigInteger::add);
      }
      return;
    }

    if (serializedData.has("items2")) {
      for (JsonElement element :
          serializedData.getJsonElement().getAsJsonObject().getAsJsonArray("items2")) {
        JsonObject elementObject = element.getAsJsonObject();

        ItemStack item = unwrap(elementObject.get("item"));
        BigInteger amount = elementObject.get("amount").getAsBigInteger();
        items.merge(BackPackItem.wrap(item), amount, BigInteger::add);
      }
    }
  }

  public BigInteger sumAllItems() {
    return items.values()
            .stream()
            .reduce(BigInteger::add)
            .orElse(BigInteger.ZERO);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BackPackData that = (BackPackData) o;
    return new EqualsBuilder().append(level, that.level).append(sell, that.sell).append(sumAllItems(), that.sumAllItems()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(sumAllItems()).append(level).append(sell).toHashCode();
  }
}
