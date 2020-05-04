package com.bgsoftware.superiorprison.plugin.menu.messages;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessages;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class MessageEditMenu extends OMenu {

    private SMineMessage message;
    private SMineMessages messages;

    public MessageEditMenu(SPrisoner viewer, SMineMessages messages, SMineMessage message) {
        super("mineMessageEdit", viewer);
        this.message = message;
        this.messages = messages;

        clickHandler("interval")
                .handle(event -> {
                    LocaleEnum.EDIT_MESSAGE_INTERVAL.getWithPrefix().send(event.getWhoClicked());
                    AtomicLong newInterval = new AtomicLong(-1);
                    previousMove = false;
                    event.getWhoClicked().closeInventory();

                    SubscriptionFactory.getInstance().subscribeTo(AsyncPlayerChatEvent.class, chatEvent ->{
                        long seconds = TimeUtil.toSeconds(chatEvent.getMessage());
                        newInterval.set(seconds);
                        message.setInterval(seconds);
                        messageBuilder(LocaleEnum.EDIT_MESSAGE_INTERVAL_SET.getWithPrefix())
                                .replace(message)
                                .send(chatEvent);

                        chatEvent.setCancelled(true);
                        previousMove = true;
                        refresh();
                    }, new SubscriptionProperties<AsyncPlayerChatEvent>().timeOut(TimeUnit.MINUTES, 2).runTill(chatEvent -> newInterval.get() != -1));
                });
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{messages.getMine(), messages, message};
    }
}
