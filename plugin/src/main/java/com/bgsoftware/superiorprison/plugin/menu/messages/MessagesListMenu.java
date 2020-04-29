package com.bgsoftware.superiorprison.plugin.menu.messages;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessage;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MessagesListMenu extends OPagedMenu<SMineMessage> implements OMenu.Templateable {

    private SNormalMine mine;
    public MessagesListMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineMessages", viewer);
        this.mine = mine;
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public List<SMineMessage> requestObjects() {
        return mine.getMessages().get().stream().map(message -> (SMineMessage) message).collect(Collectors.toList());
    }

    @Override
    public OMenuButton toButton(SMineMessage obj) {
        Optional<OMenuButton> messageTemplate = getTemplateButtonFromTemplate("message");
        if (!messageTemplate.isPresent()) return null;

        OMenuButton oMenuButton = messageTemplate.get().clone();
        oMenuButton.currentItem(oMenuButton.getDefaultStateItem().getItemStackWithPlaceholders(obj));
        return oMenuButton;
    }
}
