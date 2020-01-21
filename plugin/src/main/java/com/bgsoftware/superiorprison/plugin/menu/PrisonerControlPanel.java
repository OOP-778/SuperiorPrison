package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.ButtonClickEvent;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.oop.orangeengine.main.Helper;

public class PrisonerControlPanel extends OMenu {
    public PrisonerControlPanel(SPrisoner viewer) {
        super("prisonerControlPanel", viewer);

        getStateRequester()
                .registerRequest("auto sell", button -> getToggleableState(button, viewer.isAutoSell()))
                .registerRequest("fortune blocks", button -> getToggleableState(button, viewer.isFortuneBlocks()))
                .registerRequest("auto pickup", button -> getToggleableState(button, viewer.isAutoPickup()));

        ClickHandler
                .of("auto sell")
                .handle(event -> handleToggleable(event, viewer.isAutoSell(), () -> viewer.setAutoSell(!viewer.isAutoSell())))
                .apply(this);

        ClickHandler
                .of("fortune blocks")
                .handle(event -> handleToggleable(event, viewer.isFortuneBlocks(), () -> viewer.setFortuneBlocks(!viewer.isFortuneBlocks())))
                .apply(this);

        ClickHandler
                .of("auto pickup")
                .handle(event -> handleToggleable(event, viewer.isAutoPickup(), () -> viewer.setAutoPickup(!viewer.isAutoPickup())))
                .apply(this);
    }

    private OMenuButton.ButtonItemBuilder getToggleableState(OMenuButton button, boolean state) {
        if (state)
            return button.getStateItem("enabled");

        else
            return button.getStateItem("disabled");
    }

    public void handleToggleable(ButtonClickEvent event, Boolean current, Runnable toggle) {
        toggle.run();
        event.getWhoClicked().sendMessage(Helper.color("You've " + (current ? "disabled" : "enabled") + " '" + event.getButton().action() + "'"));
        refresh();
    }

}
