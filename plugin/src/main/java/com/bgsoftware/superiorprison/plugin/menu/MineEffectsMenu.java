package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.api.data.mine.effects.MineEffect;
import com.bgsoftware.superiorprison.plugin.object.mine.effects.SMineEffect;
import com.bgsoftware.superiorprison.plugin.object.mine.effects.SMineEffects;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.version.OVersion;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MineEffectsMenu extends OPagedMenu<PotionEffectType> implements OMenu.Templateable {

    private final SMineEffects effects;

    public MineEffectsMenu(SPrisoner viewer, SMineEffects effects) {
        super("mineEffects", viewer);
        this.effects = effects;

        clickHandler("effect")
                .handle(event -> {
                    PotionEffectType potionType = requestObject(event.getRawSlot());
                    MineEffect mineEffect = effects.get(potionType).orElse(null);
                    effects.clearEffects();

                    if (event.getClick().name().contains("LEFT")) {
                        // Increase
                        if (mineEffect == null) {
                            mineEffect = new SMineEffect(potionType, 1);
                            effects.add((SMineEffect) mineEffect);
                        } else
                            mineEffect.setAmplifier(mineEffect.getAmplifier() + 1);

                        refresh();
                        effects.getMine().save(true);
                    } else {
                        // Decrease
                        if (mineEffect == null) return;

                        mineEffect.setAmplifier(mineEffect.getAmplifier() - 1);
                        if (mineEffect.getAmplifier() == 0) {
                            effects.remove(mineEffect);
                        }

                        refresh();
                        effects.getMine().save(true);
                    }

                    effects.reapplyEffects();
                });
    }

    @Override
    public List<PotionEffectType> requestObjects() {
        return Arrays.stream(PotionEffectType.values()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public OMenuButton toButton(PotionEffectType obj) {
        try {
            Optional<OMenuButton> effect = getTemplateButtonFromTemplate("effect");
            if (!effect.isPresent()) return null;

            OMenuButton effectButton = effect.get().clone();
            OMenuButton.ButtonItemBuilder defaultStateItem = effectButton.getDefaultStateItem();
            int amplifier = 0;

            Optional<MineEffect> mineEffect = effects.get(obj);
            if (mineEffect.isPresent()) amplifier = mineEffect.get().getAmplifier();

            ItemStack itemStack = defaultStateItem.getItemStack();
            if (itemStack.getType().name().toLowerCase().contains("potion")) {
                PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();
                if (OVersion.isOrAfter(12))
                    itemMeta.setColor(obj.getColor());
                itemStack.setItemMeta(itemMeta);
            }

            defaultStateItem.itemBuilder().addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
            defaultStateItem.itemBuilder().makeGlow();

            return effectButton.currentItem(defaultStateItem.getItemStackWithPlaceholders(ImmutableMap.of("{potion_amplifier}", amplifier, "{potion_type}", Helper.beautify(obj.getName()))));
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return null;
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{effects.getMine(), effects};
    }
}
