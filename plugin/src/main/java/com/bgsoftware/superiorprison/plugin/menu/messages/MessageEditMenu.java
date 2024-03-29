package com.bgsoftware.superiorprison.plugin.menu.messages;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;
import static com.bgsoftware.superiorprison.plugin.util.TextUtil.mergeText;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineActionBarMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineChatMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessages;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineTitleMessage;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.bgsoftware.superiorprison.plugin.util.chatcmds.ChatCommands;
import com.bgsoftware.superiorprison.plugin.util.input.PlayerInput;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.entity.Player;

public class MessageEditMenu extends OMenu {

  private final SMineMessage message;
  private final SMineMessages messages;

  public MessageEditMenu(SPrisoner viewer, SMineMessages messages, SMineMessage message) {
    super("mineMessageEdit", viewer);
    this.message = message;
    this.messages = messages;

    clickHandler("interval")
        .handle(
            event -> {
              LocaleEnum.EDIT_MESSAGE_INTERVAL.getWithPrefix().send(event.getWhoClicked());
              forceClose();

              Runnable onCancel = this::refresh;
              new PlayerInput<Long>((Player) event.getWhoClicked())
                  .timeOut(TimeUnit.MINUTES, 2)
                  .parser(TimeUtil::toSeconds)
                  .onCancel(onCancel)
                  .onInput(
                      (obj, input) -> {
                        messageBuilder(LocaleEnum.EDIT_MESSAGE_INTERVAL_SET.getWithPrefix())
                            .replace(message)
                            .send(event.getWhoClicked());

                        message.setInterval(input);
                        messages.getMine().save(true);
                        messages.getMine().getLinker().call(messages);
                        obj.cancel();
                      })
                  .listen();
            });

    clickHandler("content")
        .handle(
            event -> {
              forceClose();
              ChatCommands chatCommands = new ChatCommands((Player) event.getWhoClicked());
              final AtomicReference<OMessage> localeMessage = new AtomicReference<>(null);

              Runnable sendMessage =
                  () ->
                      messageBuilder(localeMessage.get())
                          .replace(message)
                          .send(event.getWhoClicked());

              if (message instanceof SMineChatMessage || message instanceof SMineActionBarMessage) {
                boolean isChat = message instanceof SMineChatMessage;
                localeMessage.set(
                    isChat
                        ? LocaleEnum.EDIT_MESSAGE_CHAT_CONTENT.getMessage()
                        : LocaleEnum.EDIT_MESSAGE_ACTION_BAR_CONTENT.getMessage());
                chatCommands.appendCommand(
                    "set",
                    (player, args) -> {
                      String content = mergeText(args);
                      if (isChat) ((SMineChatMessage) message).setContent(content);
                      else ((SMineActionBarMessage) message).setContent(content);

                      messageBuilder(LocaleEnum.EDIT_MESSAGE_CONTENT_SUCCESS.getWithPrefix())
                          .replace(message)
                          .send(player);
                    });

              } else if (message instanceof SMineTitleMessage) {
                localeMessage.set(LocaleEnum.EDIT_MESSAGE_TITLE_CONTENT.getMessage());
                chatCommands.appendCommand(
                    "setTitle",
                    (player, args) -> {
                      String content = mergeText(args);
                      ((SMineTitleMessage) message).setTitle(content);
                      messageBuilder(LocaleEnum.EDIT_MESSAGE_TITLE_SUCCESS.getWithPrefix())
                          .replace(message)
                          .send(player);
                    });

                chatCommands.appendCommand(
                    "setSubTitle",
                    (player, args) -> {
                      String content = mergeText(args);
                      ((SMineTitleMessage) message).setSubTitle(content);
                      messageBuilder(LocaleEnum.EDIT_MESSAGE_TITLE_SUBTITLE_SUCCESS.getWithPrefix())
                          .replace(message)
                          .send(player);
                    });

                chatCommands.appendCommand(
                    "setFadeIn",
                    (player, args) -> {
                      int fadeIn = Integer.parseInt(args[0]);
                      ((SMineTitleMessage) message).setFadeIn(fadeIn);
                      messageBuilder(LocaleEnum.EDIT_MESSAGE_TITLE_FADEIN_SUCCESS.getWithPrefix())
                          .replace(message)
                          .send(player);
                    });

                chatCommands.appendCommand(
                    "setStay",
                    (player, args) -> {
                      int stay = Integer.parseInt(args[0]);
                      ((SMineTitleMessage) message).setStay(stay);
                      messageBuilder(LocaleEnum.EDIT_MESSAGE_TITLE_STAY_SUCCESS.getWithPrefix())
                          .replace(message)
                          .send(player);
                    });

                chatCommands.appendCommand(
                    "setFadeOut",
                    (player, args) -> {
                      int fadeOut = Integer.parseInt(args[0]);
                      ((SMineTitleMessage) message).setFadeOut(fadeOut);
                      messageBuilder(LocaleEnum.EDIT_MESSAGE_TITLE_FADEOUT_SUCCESS.getWithPrefix())
                          .replace(message)
                          .send(player);
                    });
              }

              chatCommands.appendCommand(
                  "cancel",
                  (player, canceller, args) -> {
                    refresh();
                    messages.getMine().save(true);
                    refresh();
                    messages.getMine().getLinker().call(messages);
                    canceller.set(true);
                  });

              chatCommands.appendCommand(
                  "save",
                  (player, canceller, args) -> {
                    messageBuilder(LocaleEnum.EDIT_MESSAGE_SAVE.getWithPrefix())
                        .replace(message)
                        .send(player);
                    messages.getMine().save(true);
                    refresh();
                    messages.getMine().getLinker().call(messages);
                    canceller.set(true);
                  });

              chatCommands.listen();
              chatCommands.onFinish(this::refresh);
              chatCommands.afterInput(sendMessage);

              chatCommands.setExceptionHandler(
                  ((player, throwable) -> {
                    OMessage clone = LocaleEnum.PREFIX_ERROR.getMessage().clone();
                    if (clone instanceof OChatMessage) {
                      ((OChatMessage) clone).lineList().get(0).append(throwable.getMessage());
                    }
                    clone.send(player);
                  }));
              sendMessage.run();
            });
  }

  @Override
  public Object[] getBuildPlaceholders() {
    return new Object[] {messages.getMine(), messages, message};
  }
}
