package com.bgsoftware.superiorprison.plugin.object.mine.linkable;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.common.collect.Sets;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.lib.google.gson.JsonArray;
import com.oop.datamodule.lib.google.gson.JsonElement;
import com.oop.datamodule.lib.google.gson.JsonObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

public class ObjectLinker implements Attachable<SNormalMine>, SerializableObject {
  private final Map<String, Set<LinkInfo>> linkedObjects = new HashMap<>();
  @Getter private final Map<String, String> linkedTo = new HashMap<>();
  private SNormalMine mine;

  public <T extends LinkableObject<T>> void link(SNormalMine to, T object) {
    Set<LinkInfo> linkInfos =
        linkedObjects.computeIfAbsent(object.getLinkId(), key -> Sets.newConcurrentHashSet());
    if (linkInfos.stream().anyMatch(i -> i.to.equalsIgnoreCase(to.getName()))) return;

    linkInfos.add(new LinkInfo(to.getName(), object));
    to.getLinker().linkedTo.put(object.getLinkId(), mine.getName());
    mine.save(true);

    to.getLinkableObjects()
        .get(object.getLinkId())
        .onChange(mine.getLinkableObjects().get(object.getLinkId()));
    to.save(true);
  }

  public void unlink(String linkId, String mineName) {
    Set<LinkInfo> linkInfos = linkedObjects.get(linkId);
    if (linkInfos == null) return;

    linkInfos.removeIf(
        object -> {
          boolean remove = object.to.contentEquals(mineName);

          if (remove) {
            if (object.cache == null) {
              object.cache =
                  (SNormalMine)
                      SuperiorPrisonPlugin.getInstance()
                          .getMineController()
                          .getMine(mineName)
                          .orElse(null);
            }

            if (object.cache != null) {
              object.cache.getLinker().linkedTo.remove(linkId, mine.getName());
              object.cache.save(true);
            }
          }
          return remove;
        });
    mine.save(true);
  }

  public <T extends LinkableObject<?>> void call(T object) {
    Set<LinkInfo> linkInfos = linkedObjects.get(object.getLinkId());
    if (linkInfos == null) return;

    for (LinkInfo linkInfo : Sets.newHashSet(linkInfos)) {

      // If mine is not cached yet, cache
      if (linkInfo.cache == null) {
        linkInfo.cache =
            (SNormalMine)
                SuperiorPrisonPlugin.getInstance()
                    .getMineController()
                    .getMine(linkInfo.to)
                    .orElse(null);
        if (linkInfo.cache == null) {
          linkInfos.remove(linkInfo);
          continue;
        }
      }

      // if the mine is removed => remove it from the linked objects
      if (!SuperiorPrisonPlugin.getInstance()
          .getMineController()
          .getMine(linkInfo.cache.getName())
          .isPresent()) {
        linkInfos.remove(linkInfo);
        continue;
      }

      // If linked object is null, get the object from linked mine
      if (linkInfo.linkedObject == null)
        linkInfo.linkedObject = linkInfo.cache.getLinkableObjects().get(object.getLinkId());

      linkInfo.linkedObject.onChange(object);
      linkInfo.cache.save(true);
    }
  }

  @Override
  public void attach(SNormalMine obj) {
    this.mine = obj;
  }

  @Override
  public void serialize(SerializedData serializedData) {
    JsonArray array = new JsonArray();
    linkedObjects.forEach(
        (linkId, objects) -> {
          JsonObject object = new JsonObject();
          object.addProperty("key", linkId);

          JsonArray infoArray = new JsonArray();
          for (LinkInfo linkInfo : objects) infoArray.add(linkInfo.to);
          object.add("value", infoArray);

          array.add(object);
        });
    serializedData.getJsonElement().getAsJsonObject().add("linkedObjects", array);

    JsonArray linkedToArray = new JsonArray();
    linkedTo.forEach(
        (linkId, mineName) -> {
          JsonObject object = new JsonObject();
          object.addProperty("key", linkId);
          object.addProperty("value", mineName);
          linkedToArray.add(object);
        });

    serializedData.getJsonElement().getAsJsonObject().add("linkedTo", linkedToArray);
  }

  @Override
  public void deserialize(SerializedData serializedData) {
    JsonArray array =
        serializedData.getJsonElement().getAsJsonObject().getAsJsonArray("linkedObjects");
    for (JsonElement jsonElement : array) {
      String linkId = jsonElement.getAsJsonObject().get("key").getAsString();
      Set<LinkInfo> linkInfos = new HashSet<>();

      JsonArray infoArray = jsonElement.getAsJsonObject().getAsJsonArray("value");
      for (JsonElement element : infoArray)
        linkInfos.add(new LinkInfo(element.getAsString(), null));

      linkedObjects.put(linkId, linkInfos);
    }

    JsonArray linkedTo =
        serializedData.getJsonElement().getAsJsonObject().getAsJsonArray("linkedTo");
    for (JsonElement jsonElement : linkedTo)
      this.linkedTo.put(
          jsonElement.getAsJsonObject().get("key").getAsString(),
          jsonElement.getAsJsonObject().get("value").getAsString());
  }

  public boolean isLinked(String linkId) {
    return linkedTo.containsKey(linkId);
  }

  private class LinkInfo {
    private final String to;
    private LinkableObject linkedObject;

    private SNormalMine cache;

    private LinkInfo(String to, LinkableObject<?> linkedObject) {
      this.to = to;
      this.linkedObject = linkedObject;
    }
  }
}
