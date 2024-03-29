package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MineActionBarMessage;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineChatMessage;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineMessage;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineMesssages;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineTitleMessage;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.NonNull;

public class SMineMessages
    implements MineMesssages,
        SerializableObject,
        Attachable<SNormalMine>,
        LinkableObject<SMineMessages> {

  private final Map<Integer, SMineMessage> messages = new HashMap<>();

  @Getter private SNormalMine mine;

  @Override
  public Optional<MineMessage> get(int id) {
    return Optional.ofNullable(messages.get(id));
  }

  @Override
  public Set<MineMessage> get() {
    return new HashSet<>(messages.values());
  }

  @Override
  public void remove(int id) {
    messages.remove(id);
  }

  @Override
  public MineChatMessage addChatMessage(String content, long every) {
    return generateId(new SMineChatMessage(every, content));
  }

  @Override
  public MineTitleMessage addTitleMessage(
      int fadeIn, int stay, int fadeOut, String title, String subTitle, long every) {
    return generateId(new SMineTitleMessage(every, fadeIn, fadeOut, stay, title, subTitle));
  }

  @Override
  public MineActionBarMessage addActionBarMessage(String content, long every) {
    return generateId(new SMineActionBarMessage(every, content));
  }

  public SMineMessage add(@NonNull SMineMessage message) {
    generateId(message);
    messages.put(message.getId(), message);
    return message;
  }

  public <T extends SMineMessage> T generateId(T message) {
    int[] ids = new int[3];
    for (int i = 0; i < ids.length; i++) ids[i] = ThreadLocalRandom.current().nextInt(9);

    message.assignId(Integer.parseInt("" + ids[0] + ids[1] + ids[2]));
    return message;
  }

  @Override
  public void serialize(SerializedData serializedData) {
    serializedData.write("messages", messages.values());
  }

  @Override
  public void deserialize(SerializedData serializedData) {
    serializedData
        .applyAsCollection("messages")
        .map(
            element -> {
              if (!element.getJsonElement().isJsonObject()) return null;
              return SMineMessage.from(element);
            })
        .filter(Objects::nonNull)
        .forEach(message -> messages.put(message.getId(), message));
  }

  @Override
  public void attach(SNormalMine obj) {
    this.mine = obj;
  }

  @Override
  public void onChange(SMineMessages from) {
    messages.clear();
    from.messages.forEach((key, message) -> messages.put(key, message.clone()));
  }

  @Override
  public String getLinkId() {
    return "messages";
  }
}
