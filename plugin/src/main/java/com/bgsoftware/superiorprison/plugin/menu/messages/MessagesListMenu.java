package com.bgsoftware.superiorprison.plugin.menu.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MessageType;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineActionBarMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineChatMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineTitleMessage;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class MessagesListMenu extends OPagedMenu<SMineMessage> implements OMenu.Templateable {

    private final SNormalMine mine;

    public MessagesListMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineMessages", viewer);
        this.mine = mine;

        clickHandler("message")
                .handle(event -> {
                    SMineMessage message = requestObject(event.getRawSlot());
                    if (event.getClick().isLeftClick()) {
                        move(new MessageEditMenu(viewer, mine.getMessages(), message));

                    } else {
                        mine.getMessages().remove(message);
                        mine.save(true);
                        refresh();
                    }
                });

        clickHandler("create")
                .handle(event -> {
                    previousMove = false;
                    event.getWhoClicked().closeInventory();
                    LocaleEnum.MINE_MESSAGES_CREATE_TYPE.getWithPrefix().send(event.getWhoClicked());
                    SubscriptionFactory.getInstance().subscribeTo(AsyncPlayerChatEvent.class, chatEvent1 -> {
                        MessageType match = MessageType.match(chatEvent1.getMessage());
                        chatEvent1.setCancelled(true);
                        LocaleEnum.MINE_MESSAGES_CREATE_INTERVAL.getWithPrefix().send(chatEvent1.getPlayer());

                        SubscriptionFactory.getInstance().subscribeTo(AsyncPlayerChatEvent.class, chatEvent2 -> {
                            long seconds = TimeUtil.toSeconds(chatEvent2.getMessage());

                            SMineMessage message = null;
                            if (match == MessageType.CHAT)
                                message = new SMineChatMessage(seconds, null);

                            else if (match == MessageType.ACTION_BAR)
                                message = new SMineActionBarMessage(seconds, null);

                            else if (match == MessageType.TITLE)
                                message = new SMineTitleMessage(seconds, 5, 10, 5, null, null);

                            mine.getMessages().add(message);
                            mine.save(true);
                            messageBuilder(LocaleEnum.MINE_MESSAGES_CREATE_SUCCESS.getWithPrefix())
                                    .replace(message)
                                    .send(chatEvent2);
                            chatEvent2.setCancelled(true);

                            move(new MessageEditMenu(viewer, mine.getMessages(), message));

                        }, new SubscriptionProperties<AsyncPlayerChatEvent>().timesToRun(1));
                    }, new SubscriptionProperties<AsyncPlayerChatEvent>().timesToRun(1).filter(chatEvent1 -> chatEvent1.getMessage().equalsIgnoreCase("actionbar") || chatEvent1.getMessage().equalsIgnoreCase("title") || chatEvent1.getMessage().equalsIgnoreCase("chat")));
                });
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

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{mine, mine.getMessages()};
    }
}
