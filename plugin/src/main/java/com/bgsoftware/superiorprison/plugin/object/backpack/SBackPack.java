package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.api.data.backpack.BackPackType;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.google.common.base.Preconditions;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.lib.google.gson.JsonObject;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static com.bgsoftware.superiorprison.plugin.controller.SBackPackController.NBT_KEY;
import static com.bgsoftware.superiorprison.plugin.controller.SBackPackController.UUID_KEY;

@AllArgsConstructor
public class SBackPack implements BackPack {

  private static final Pattern BACKPACK_DATA_PATTERN = Pattern.compile(NBT_KEY + "_([0-9]+)");
  private final Object lock = false;
  @Getter private BackPackData data;
  private NBTItem nbtItem;
  private ItemStack itemStack;
  @Getter private Player holder;
  private JsonObject oldData;
  // @Setter @Getter private AdvancedBackPackView currentView;
  private int hashcode;
  @Getter @Setter private long lastUpdated = -1;
  @Getter private UUID uuid;
  @Getter private BackPackType type;
  @Getter @Setter private BigInteger used = BigInteger.ZERO;
  @Setter @Getter private int currentSlot = -1;

  private SBackPack() {}

  @SneakyThrows
  public SBackPack(@Nonnull ItemStack itemStack, Player player) {
    this.holder = player;
    this.itemStack = itemStack;
    this.nbtItem = new NBTItem(itemStack);

    Preconditions.checkArgument(
        nbtItem.getKeys().stream().anyMatch(in -> in.startsWith(NBT_KEY)),
        "The given item is not an backpack");

    String serialized = uncoverData(nbtItem);
    try {
      serialized = decompress(serialized);
    } catch (Exception ignored) {
    }

    oldData = StorageInitializer.getInstance().getGson().fromJson(serialized, JsonObject.class);

    // Initialize UUID
    String stringUUID = nbtItem.getString(UUID_KEY);
    if (stringUUID != null && stringUUID.trim().length() != 0) uuid = UUID.fromString(stringUUID);
    else uuid = UUID.randomUUID();

    JsonObject localData;
    // Check if this is an outdated bool nbt value
    if (SuperiorPrisonPlugin.getInstance().getBackPackController().isPlayerBound()) {
      if (oldData.has("global")) localData = oldData.getAsJsonObject("global");
      else localData = oldData.getAsJsonObject(player.getUniqueId().toString());

    } else {
      if (!oldData.has("global"))
        localData = oldData.getAsJsonObject(player.getUniqueId().toString());
      else localData = oldData.getAsJsonObject("global");
    }

    data = new BackPackData(this);
    if (localData != null) data.deserialize(new SerializedData(localData));
    this.used = data.sumAllItems();

    currentSlot = player.getInventory().first(itemStack);

    BackPackConfig config =
        SuperiorPrisonPlugin.getInstance()
            .getBackPackController()
            .getConfig(data.getConfigId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Failed to find backPack by id "
                            + data.getConfigId()
                            + " level "
                            + data.getLevel()))
            .getByLevel(data.getLevel());

    this.type =
        config.getType().equalsIgnoreCase("advanced") ? BackPackType.ADVANCED : BackPackType.SIMPLE;
    updateHash();
    save();
    update();
  }

  public static SBackPack of(BackPackConfig config, Player player) {
    SBackPack backPack = new SBackPack();
    backPack.oldData = new JsonObject();
    backPack.data = new BackPackData(backPack);
    backPack.data.setSell(config.isSellByDefault());
    backPack.data.setLevel(1);
    backPack.data.setConfigId(config.getId());
    backPack.holder = player;
    backPack.itemStack = config.getItem().getItemStack().clone();
    backPack.nbtItem = new NBTItem(backPack.itemStack);
    backPack.uuid = UUID.randomUUID();

    backPack.save();
    backPack.updateHash();
    return backPack;
  }

  public static String[] chunkArray(char[] array, int numOfChunks) {
    int chunkSize = (int) Math.ceil((double) array.length / numOfChunks);
    String[] output = new String[numOfChunks];

    for (int i = 0; i < numOfChunks; i++) {
      int start = i * chunkSize;
      int length = Math.min(array.length - start, chunkSize);

      char[] temp = new char[length];
      System.arraycopy(array, start, temp, 0, length);
      output[i] = new String(temp);
    }

    return output;
  }

  public static String compress(byte[] bytes) throws Exception {
    Deflater deflater = new Deflater();
    deflater.setInput(bytes);
    deflater.finish();
    ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
    byte[] buffer = new byte[1024];
    while (!deflater.finished()) {
      int count = deflater.deflate(buffer);
      bos.write(buffer, 0, count);
    }
    bos.close();
    byte[] output = bos.toByteArray();
    return encodeBase64(output);
  }

  public static String decompress(String string) throws Exception {
    byte[] bytes = decodeBase64(string);
    Inflater inflater = new Inflater();
    inflater.setInput(bytes);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
    byte[] buffer = new byte[1024];

    while (!inflater.finished()) {
      int count = inflater.inflate(buffer);
      bos.write(buffer, 0, count);
    }

    bos.close();
    return bos.toString();
  }

  public static String encodeBase64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes).replace("\r\n", "").replace("\n", "");
  }

  public static byte[] decodeBase64(String str) {
    return Base64.getDecoder().decode(str);
  }

  private void updateHash() {
    this.hashcode = data.hashCode();
  }

  @Override
  public BigInteger getCapacity() {
    return getConfig().getCapacity();
  }

  @Override
  public int getCurrentLevel() {
    return data.getLevel();
  }

  @Override
  public Map<ItemStack, BigInteger> getStored() {
    return data.getItems()
            .entrySet()
            .stream()
            .map(entry -> new OPair<>(entry.getKey().getItemStack(), entry.getValue()))
            .collect(Collectors.toMap(OPair::getKey, OPair::getValue));
  }

  @Override
  public String getId() {
    return data.getConfigId();
  }

  public void updateNbt() {
    ItemBuilder<?> oItem = getConfig().getItem().clone().makeUnstackable();
    List<String> oldLore = oItem.getLore();
    List<String> newLore = new ArrayList<>();

    for (String line : oldLore) {
      if (line.startsWith("{item_template}")) {
        line = line.replace("{item_template}", "");
        Map<BackPackItem, BigInteger> contents = data.getItems();

        String finalLine = line;
        contents.forEach(
            (item, amount) -> {
              newLore.add(
                  finalLine
                      .replace("{item_type}", TextUtil.beautify(item.getMaterial()))
                      .replace("{item_amount}", amount + ""));
            });
      } else newLore.add(line);
    }

    oItem.setLore(newLore);
    oItem.replace("{backpack_level}", getCurrentLevel());
    oItem.replace("{backpack_capacity}", getCapacity());
    oItem.replace("{backpack_used}", getUsed());
    this.nbtItem = new NBTItem(oItem.getItemStack());
  }

  @Override
  public ItemStack getItem() {
    return nbtItem.getItem();
  }

  public BackPackConfig getConfig() {
    return SuperiorPrisonPlugin.getInstance()
        .getBackPackController()
        .getConfig(data.getConfigId())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Failed to find backpack by id: "
                        + data.getConfigId()
                        + " level: "
                        + data.getLevel()))
        .getByLevel(data.getLevel());
  }

  @SneakyThrows
  @Override
  public synchronized void save() {
    if (!isModified()) return;

    updateNbt();
    SerializedData serializedData = new SerializedData();
    data.serialize(serializedData);

    if (SuperiorPrisonPlugin.getInstance().getBackPackController().isPlayerBound()) {
      oldData.remove(holder.getUniqueId().toString());
      oldData.add(holder.getUniqueId().toString(), serializedData.getJsonElement());

    } else {
      oldData.remove("global");
      oldData.add("global", serializedData.getJsonElement());
    }

    String data = StorageInitializer.getInstance().getGson().toJson(oldData);

    // Compress the data
    data = compress(data.getBytes(StandardCharsets.UTF_8));

    if (data.length() > 32767) {
      String[] strings = splitData(data);
      for (int i = 0; i < strings.length; i++) nbtItem.setString(NBT_KEY + "_" + i, strings[i]);

    } else nbtItem.setString(NBT_KEY, data);

    nbtItem.setString(UUID_KEY, uuid.toString());
    updateHash();
  }

  @Override
  public Map<ItemStack, BigInteger> add(ItemStack... itemStacks) {
    Map<BackPackItem, BigInteger> notAddedItems = new HashMap<>();

    for (ItemStack stack : itemStacks)
      notAddedItems.merge(
          BackPackItem.wrap(stack), BigInteger.valueOf(stack.getAmount()), BigInteger::add);

    Supplier<Map<ItemStack, BigInteger>> convertBackPackPackItems =
        () ->
            notAddedItems.entrySet().stream()
                .map(entry -> new OPair<>(entry.getKey().getItemStack(), entry.getValue()))
                .collect(Collectors.toMap(OPair::getKey, OPair::getValue));

    synchronized (lock) {
      for (ItemStack stack : itemStacks) {
        // Make sure we don't exceed the limit
        if (isFull()) return convertBackPackPackItems.get();

        BackPackItem wrappedItem = BackPackItem.wrap(stack);
        boolean mapContains = data.getItems().containsKey(wrappedItem);

        BigInteger stackAmount = BigInteger.valueOf(stack.getAmount());
        BigInteger canStore = getCapacity().subtract(getUsed());
        BigInteger toAdd = canStore.min(stackAmount);
        if (NumberUtil.equals(toAdd, BigInteger.ZERO)) continue;

        this.used = used.add(toAdd);

        if (mapContains)
          data.getItems().merge(wrappedItem, toAdd, BigInteger::add);
        else
          data.getItems().put(wrappedItem, toAdd);

        BigInteger merge = notAddedItems.merge(wrappedItem, toAdd, BigInteger::subtract);
        if (NumberUtil.equals(merge, BigInteger.ZERO)) {
          notAddedItems.remove(wrappedItem);
        }
      }
    }

    // if (currentView != null) currentView.onUpdate();
    return convertBackPackPackItems.get();
  }

  @Override
  public Map<ItemStack, BigInteger> remove(ItemStack... itemStacks) {
    Map<BackPackItem, BigInteger> notRemovedItems = new HashMap<>();
    for (ItemStack stack : itemStacks)
      notRemovedItems.merge(
          BackPackItem.wrap(stack), BigInteger.valueOf(stack.getAmount()), BigInteger::add);

    Supplier<Map<ItemStack, BigInteger>> convertBackPackPackItems =
        () ->
            notRemovedItems.entrySet().stream()
                .map(entry -> new OPair<>(entry.getKey().getItemStack(), entry.getValue()))
                .collect(Collectors.toMap(OPair::getKey, OPair::getValue));
    synchronized (lock) {
      if (NumberUtil.equals(getUsed(), BigInteger.ZERO)) return convertBackPackPackItems.get();

      for (ItemStack stack : itemStacks) {
        BackPackItem wrappedItem = BackPackItem.wrap(stack);
        BigInteger amount = BigInteger.valueOf(stack.getAmount());

        boolean mapContains = data.getItems().containsKey(wrappedItem);
        if (mapContains) {
          BigInteger merged =
              data.getItems()
                  .merge(
                      wrappedItem,
                      amount,
                      (current, remove) -> {
                        if (NumberUtil.isMoreOrEquals(current, remove))
                          return current.subtract(remove);

                        return current.negate();
                      });

          // If remove was bigger than the amount
          if (NumberUtil.isLessThan(merged, BigInteger.ZERO)) {
            merged = notRemovedItems.merge(wrappedItem, merged, BigInteger::add);
            this.used = used.add(merged);

          } else {
            merged = BigInteger.ZERO;
            data.getItems().remove(wrappedItem);

            this.used = used.subtract(amount);
          }

          // If it removed everything
          if (NumberUtil.equals(merged, BigInteger.ZERO)) {
            notRemovedItems.remove(wrappedItem);
          }
        }
      }
    }

    // if (currentView != null) currentView.onUpdate();
    return convertBackPackPackItems.get();
  }

  @Override
  public void update() {
    ItemStack newItem = getItem();

    // Update the inventory
    if (currentSlot != -1)
      if (holder.getInventory() instanceof PatchedInventory) {
        ((PatchedInventory) this.holder.getInventory()).setOwnerCalling();
        this.holder.getInventory().setItem(currentSlot, newItem);

      } else holder.getInventory().setItem(currentSlot, newItem);

    // Update the menu
    // if (currentView != null) currentView.refresh();
    lastUpdated = System.currentTimeMillis();
  }

  public ItemStack updateManually() {
    return getItem();
  }

  @Override
  public void upgrade(int level) {
    data.setLevel(level);

    // if (currentView != null) currentView.onUpgrade();
  }

  @Override
  public boolean isModified() {
    return data.hashCode() != hashcode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    return uuid.equals(((SBackPack) o).uuid);
  }

  @Override
  public int hashCode() {
    return uuid.hashCode();
  }

  @Override
  public boolean isFull() {
    return NumberUtil.equals(getCapacity(), getUsed());
  }

  private String uncoverData(NBTItem nbtItem) {
    List<OPair<Integer, String>> uncoveredData = new ArrayList<>();

    // Extracting data
    for (String key : nbtItem.getKeys()) {
      Matcher matcher = BACKPACK_DATA_PATTERN.matcher(key);
      if (matcher.find()) {
        uncoveredData.add(new OPair<>(Integer.parseInt(matcher.group(1)), nbtItem.getString(key)));
      }
    }

    if (uncoveredData.isEmpty()) return nbtItem.getString(NBT_KEY);

    // Sorting by the num
    uncoveredData.sort(Comparator.comparingInt(OPair::getFirst));
    String string = "";
    for (OPair<Integer, String> uncoveredDatum : uncoveredData)
      string += uncoveredDatum.getSecond();

    return string;
  }

  @SneakyThrows
  private String[] splitData(String plainData) {
    char[] initialArray = plainData.toCharArray();
    int length = initialArray.length;
    int chunks = (int) Math.ceil(length / 32767.0);

    return chunkArray(initialArray, chunks);
  }
}
