package com.bgsoftware.superiorprison.plugin.menu.top;

import com.bgsoftware.superiorprison.plugin.commands.args.TopTypeArg;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.top.STopEntry;
import com.bgsoftware.superiorprison.plugin.object.top.STopSystem;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.item.custom.OSkull;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class TopMenu extends OPagedMenu<STopEntry> implements OMenu.Templateable {
    private final STopSystem system;
    private final TopTypeArg.TopType type;

    public TopMenu(SPrisoner viewer, TopTypeArg.TopType type, STopSystem system) {
        super("topMenu", viewer);
        this.system = system;
        this.type = type;
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        getTemplateButtonFromTemplate("no entry").ifPresent(button -> {
            for (Integer emptySlot : getEmptySlots()) {
                inventory.setItem(emptySlot, button.getDefaultStateItem().getItemStack().clone());
            }
        });

        return inventory;
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{system, type};
    }

    @Override
    public List<STopEntry> requestObjects() {
        return system.getEntries();
    }

    @Override
    public OMenuButton toButton(STopEntry obj) {
        OMenuButton.ButtonItemBuilder builder = null;
        OMenuButton button = getTemplateButtonFromTemplate("entry").get().clone();

        switch (type) {
            case BLOCKS:
                builder = button.getStateItem("blocks entry");
                break;
            case PRESTIGE:
                builder = button.getStateItem("prestige entry");
                break;
        }

        if (builder.itemBuilder() instanceof OSkull) {
            if (((OSkull) builder.itemBuilder()).texture().equalsIgnoreCase("{entry_texture}"))
                ((OSkull) builder.itemBuilder()).texture(obj.getPrisoner().getTextureValue());
        }

        return button.currentItem(builder.getItemStackWithPlaceholdersMulti(obj, obj.getObject(), obj.getPrisoner()));
    }

    @Override
    public OMenu getMenu() {
        return this;
    }
}
