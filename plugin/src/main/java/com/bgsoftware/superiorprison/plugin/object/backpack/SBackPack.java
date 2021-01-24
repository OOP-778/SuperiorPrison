package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.AdvancedBackPackConfig;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.bgsoftware.superiorprison.plugin.config.backpack.SimpleBackPackConfig;
import com.bgsoftware.superiorprison.plugin.menu.backpack.AdvancedBackPackView;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.google.common.base.Preconditions;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.lib.google.gson.JsonObject;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static com.bgsoftware.superiorprison.plugin.controller.SBackPackController.NBT_KEY;
import static com.bgsoftware.superiorprison.plugin.controller.SBackPackController.UUID_KEY;

@AllArgsConstructor
public class SBackPack implements BackPack {

    @Getter
    private BackPackData data;
    private NBTItem nbtItem;
    private ItemStack itemStack;

    @Getter
    private Player owner;

    private JsonObject oldData;

    @Setter
    @Getter
    private AdvancedBackPackView currentView;
    private int hashcode;

    @Getter
    @Setter
    private long lastUpdated = -1;

    // Advanced Backpack Data
    private int lastRows = -1;
    private int lastPages = -1;

    @Setter
    @Getter
    private int currentSlot = -1;

    @Getter
    private UUID uuid;

    private final Object lock = false;

    private SBackPack() {
    }

    @SneakyThrows
    public SBackPack(@Nonnull ItemStack itemStack, Player player) {
        this.owner = player;
        this.itemStack = itemStack;
        this.nbtItem = new NBTItem(itemStack);

        this.currentSlot = player.getInventory().first(itemStack);
        Preconditions.checkArgument(nbtItem.getKeys().stream().anyMatch(in -> in.startsWith(NBT_KEY)), "The given item is not an backpack");

        String serialized = uncoverData(nbtItem);
        try {
            serialized = decompress(serialized);
        } catch (Exception ignored) {
        }

        oldData = StorageInitializer.getInstance().getGson().fromJson(serialized, JsonObject.class);

        // Initialize UUID
        String stringUUID = nbtItem.getString(UUID_KEY);
        if (stringUUID != null && stringUUID.trim().length() != 0)
            uuid = UUID.fromString(stringUUID);
        else
            uuid = UUID.randomUUID();

        JsonObject localData;
        // Check if this is an outdated bool nbt value
        if (SuperiorPrisonPlugin.getInstance().getBackPackController().isPlayerBound()) {
            if (oldData.has("global"))
                localData = oldData.getAsJsonObject("global");
            else
                localData = oldData.getAsJsonObject(player.getUniqueId().toString());

        } else {
            if (!oldData.has("global"))
                localData = oldData.getAsJsonObject(player.getUniqueId().toString());
            else
                localData = oldData.getAsJsonObject("global");
        }

        data = new BackPackData(this);
        if (localData != null)
            data.deserialize(new SerializedData(localData));

        BackPackConfig<?> config = SuperiorPrisonPlugin.getInstance().getBackPackController().getConfig(data.getConfigId()).orElseThrow(() -> new IllegalStateException("Failed to find backPack by id " + data.getConfigId() + " level " + data.getLevel())).getByLevel(data.getLevel());
        if (config instanceof AdvancedBackPackConfig) {
            data.updateDataAdvanced(((AdvancedBackPackConfig) config).getRows(), ((AdvancedBackPackConfig) config).getRows(), ((AdvancedBackPackConfig) config).getPages(), ((AdvancedBackPackConfig) config).getPages());
            lastPages = ((AdvancedBackPackConfig) config).getPages();
            lastRows = ((AdvancedBackPackConfig) config).getRows();

        } else
            data.updateData();

        updateHash();
        save();
        update();
    }

    public static SBackPack of(BackPackConfig<?> config, Player player) {
        SBackPack backPack = new SBackPack();
        backPack.oldData = new JsonObject();
        backPack.data = new BackPackData(backPack);
        backPack.data.setSell(config.isSellByDefault());
        backPack.data.setLevel(1);
        backPack.data.setConfigId(config.getId());
        backPack.owner = player;
        backPack.itemStack = config.getItem().getItemStack().clone();
        backPack.nbtItem = new NBTItem(backPack.itemStack);
        backPack.uuid = UUID.randomUUID();

        if (config instanceof AdvancedBackPackConfig) {
            backPack.lastRows = ((AdvancedBackPackConfig) config).getRows();
            backPack.lastPages = ((AdvancedBackPackConfig) config).getPages();
        }

        backPack.save();
        backPack.updateHash();
        return backPack;
    }

    private void updateHash() {
        this.hashcode = data.hashCode();
    }

    @Override
    public int getCapacity() {
        return getConfig().getCapacity();
    }

    public int getSlots() {
        return getConfig().getCapacity() / 64;
    }

    @Override
    public int getCurrentLevel() {
        return data.getLevel();
    }

    @Override
    public int getUsed() {
        int used = 0;
        for (int i = 0; i < data.getStored().length; i++) {
            ItemStack itemStack = data.getStored()[i];
            if (itemStack == null) continue;

            used += itemStack.getAmount();
        }

        return used;
    }

    @Override
    public List<ItemStack> getStored() {
        return Arrays.asList(data.getStored());
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
                Map<OMaterial, Integer> contents = new HashMap<>();
                for (ItemStack stack : data.getStored()) {
                    if (stack == null || stack.getType() == Material.AIR) continue;

                    OMaterial oMaterial = OMaterial.matchMaterial(stack);
                    contents.putIfAbsent(oMaterial, 0);
                    contents.computeIfPresent(oMaterial, (oldValue, newValue) -> newValue + stack.getAmount());
                }

                String finalLine = line;
                contents.forEach((material, amount) -> {
                    newLore.add(finalLine.replace("{item_type}", TextUtil.beautify(material.name())).replace("{item_amount}", amount + ""));
                });
            } else
                newLore.add(line);
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

    public BackPackConfig<?> getConfig() {
        return SuperiorPrisonPlugin.getInstance().getBackPackController().getConfig(data.getConfigId()).orElseThrow(() -> new IllegalStateException("Failed to find backpack by id: " + data.getConfigId() + " level: " + data.getLevel())).getByLevel(data.getLevel());
    }

    @SneakyThrows
    @Override
    public synchronized void save() {
        if (!isModified()) return;

        updateNbt();
        SerializedData serializedData = new SerializedData();
        data.serialize(serializedData);

        if (SuperiorPrisonPlugin.getInstance().getBackPackController().isPlayerBound()) {
            oldData.remove(owner.getUniqueId().toString());
            oldData.add(owner.getUniqueId().toString(), serializedData.getJsonElement());

        } else {
            oldData.remove("global");
            oldData.add("global", serializedData.getJsonElement());
        }

        String data = StorageInitializer.getInstance().getGson().toJson(oldData);


        // Compress the data
        data = compress(data.getBytes(StandardCharsets.UTF_8));

        if (data.length() > 32767) {
            String[] strings = splitData(data);
            for (int i = 0; i < strings.length; i++)
                nbtItem.setString(NBT_KEY + "_" + i, strings[i]);

        } else
            nbtItem.setString(NBT_KEY, data);

        nbtItem.setString(UUID_KEY, uuid.toString());
        updateHash();
    }

    @Override
    public Map<ItemStack, Integer> add(ItemStack... itemStacks) {
        Map<ItemStack, Integer> addedItems = new HashMap<>();
        synchronized (lock) {
            Optional<OPair<Integer, ItemStack>> firstNonNull = data.firstNonNull();
            if (!firstNonNull.isPresent() && getCapacity() == getUsed())
                return Arrays.stream(itemStacks).collect(Collectors.toMap(item -> item, item -> 0));

            for (ItemStack itemStack : itemStacks) {
                if (itemStack == null) continue;
                int startingAmount = itemStack.getAmount();
                int added = 0;

                while (itemStack.getAmount() != 0) {
                    Optional<OPair<Integer, ItemStack>> similar = data.findSimilar(itemStack, true);
                    if (similar.isPresent()) {
                        // We have to check if backpack can fit more :)
                        int backpackCanAdd = getCapacity() - getUsed();
                        if (backpackCanAdd <= 0) break;

                        ItemStack itemClone = itemStack.clone();
                        if (backpackCanAdd < itemStack.getAmount())
                            itemClone.setAmount(backpackCanAdd);

                        ItemStack slotItem = similar.get().getSecond();

                        int currentAmount = slotItem.getAmount();
                        int canAdd = slotItem.getMaxStackSize() - currentAmount;

                        int adding = Math.min(canAdd, itemClone.getAmount());
                        slotItem.setAmount(slotItem.getAmount() + adding);
                        itemStack.setAmount(itemStack.getAmount() - adding);
                        added += adding;

                    } else {
                        Optional<OPair<Integer, ItemStack>> firstNull = data.firstNull();
                        if (!firstNull.isPresent()) {
                            if (!(getConfig() instanceof SimpleBackPackConfig)) break;

                            // Check how much we can still add
                            int canFit = getCapacity() - getUsed();
                            if (canFit <= 0) break;

                            int i = data.allocateMore();
                            if (canFit > itemStack.getAmount()) {
                                added += itemStack.getAmount();
                                data.setItem(i, itemStack.clone());
                                itemStack.setAmount(0);

                            } else {
                                int adding = itemStack.getAmount() - canFit;
                                ItemStack sloItem = itemStack.clone();
                                sloItem.setAmount(adding);
                                data.setItem(i, sloItem);

                                itemStack.setAmount(itemStack.getAmount() - adding);
                                added += adding;
                            }
                        } else {
                            added += itemStack.getAmount();
                            data.setItem(firstNull.get().getFirst(), itemStack.clone());
                            itemStack.setAmount(0);
                        }
                    }
                }

                if (added != startingAmount)
                    addedItems.put(itemStack, added);
            }
        }

        if (currentView != null)
            currentView.onUpdate();

        return addedItems;
    }

    @Override
    public Map<ItemStack, Integer> remove(ItemStack... itemStacks) {
        Map<ItemStack, Integer> removedItems = new HashMap<>();
        synchronized (lock) {
            if (getUsed() == 0)
                return Arrays.stream(itemStacks).collect(Collectors.toMap(item -> item, item -> 0));

            for (ItemStack itemStack : itemStacks) {
                int startingAmount = itemStack.getAmount();
                int removed = 0;

                while (itemStack.getAmount() > 0) {
                    Optional<OPair<Integer, ItemStack>> similar = data.findSimilar(itemStack, false);
                    if (!similar.isPresent()) break;

                    ItemStack slotItem = similar.get().getSecond();
                    if (slotItem.getAmount() == itemStack.getAmount()) {
                        removed += itemStack.getAmount();
                        itemStack.setAmount(0);
                        data.setItem(similar.get().getFirst(), null);

                    } else if (slotItem.getAmount() > itemStack.getAmount()) {
                        int removing = slotItem.getAmount() - itemStack.getAmount();
                        removed = itemStack.getAmount();
                        slotItem.setAmount(removing);
                        itemStack.setAmount(0);

                    } else if (slotItem.getAmount() < itemStack.getAmount()) {
                        int canRemove = itemStack.getAmount() - slotItem.getAmount();
                        data.setItem(similar.get().getFirst(), null);

                        itemStack.setAmount(canRemove);
                    }
                }
                if (startingAmount != removed)
                    removedItems.put(itemStack, removed);
            }
        }

        if (currentView != null)
            currentView.onUpdate();

        return removedItems;
    }

    @Override
    public void update() {
        ItemStack newItem = getItem();

        // Update the inventory
        if (currentSlot != -1)
            if (owner.getInventory() instanceof PatchedInventory) {
                ((PatchedInventory) this.owner.getInventory()).setOwnerCalling();
                this.owner.getInventory().setItem(currentSlot, newItem);

            } else
                owner.getInventory().setItem(currentSlot, newItem);

        // Update the menu
        if (currentView != null)
            currentView.refresh();

        lastUpdated = System.currentTimeMillis();
    }

    public ItemStack updateManually() {
        return getItem();
    }

    @Override
    public void upgrade(int level) {
        BackPackConfig<?> config = getConfig().getByLevel(level);
        data.setLevel(level);

        if (config instanceof AdvancedBackPackConfig) {
            data.updateDataAdvanced(lastRows, ((AdvancedBackPackConfig) config).getRows(), lastPages, ((AdvancedBackPackConfig) config).getPages());
            lastPages = ((AdvancedBackPackConfig) config).getPages();
            lastRows = ((AdvancedBackPackConfig) config).getRows();

        } else
            data.updateData();

        if (currentView != null)
            currentView.onUpgrade();
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
        return getCapacity() == getUsed();
    }

    private static Pattern BACKPACK_DATA_PATTERN = Pattern.compile(NBT_KEY + "_([0-9]+)");

    private String uncoverData(NBTItem nbtItem) {
        List<OPair<Integer, String>> uncoveredData = new ArrayList<>();

        // Extracting data
        for (String key : nbtItem.getKeys()) {
            Matcher matcher = BACKPACK_DATA_PATTERN.matcher(key);
            if (matcher.find()) {
                uncoveredData.add(new OPair<>(Integer.parseInt(matcher.group(1)), nbtItem.getString(key)));
            }
        }

        if (uncoveredData.isEmpty())
            return nbtItem.getString(NBT_KEY);

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
        byte[] output = bos.toByteArray();
        return new String(output);
    }

    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes).replace("\r\n", "").replace("\n", "");
    }

    public static byte[] decodeBase64(String str) {
        return Base64.getDecoder().decode(str);
    }
}
