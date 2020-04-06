package com.bgsoftware.superiorprison.plugin.menu.access;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SSpecialRank;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public abstract class AccessMenu extends OPagedMenu<AccessObject> implements OMenu.Templateable {

    private SortMethod sortMethod = SortMethod.ABC;
    private String input = null;

    public AccessMenu(String identifier, SPrisoner viewer) {
        super(identifier, viewer);

        ClickHandler
                .of("sort click")
                .handle(event -> {
                    SortMethod next = sortMethod;
                    if (event.getClick().isLeftClick())
                        next = sortMethod.getNext();

                    else if (event.getClick().isRightClick())
                        next = sortMethod.getPrevious();

                    if (next == SortMethod.INPUT) {
                        previousMove = false;
                        event.getWhoClicked().closeInventory();
                        LocaleEnum.MINE_RANK_FIND_INPUT.getWithPrefix().send((Player) event.getWhoClicked());

                        SubscriptionFactory.getInstance().subscribeTo(AsyncPlayerChatEvent.class, inputEvent -> {
                            inputEvent.setCancelled(true);
                            input = inputEvent.getMessage().toLowerCase();

                            refresh();
                            previousMove = true;
                        }, new SubscriptionProperties<AsyncPlayerChatEvent>().timesToRun(1));
                    }

                    sortMethod = next;
                    if (sortMethod != SortMethod.INPUT)
                        refresh();
                })
                .apply(this);
    }

    public List<AccessObject> sorted(Stream<AccessObject> stream) {
        if (sortMethod == SortMethod.ABC)
            stream = stream.sorted(Comparator.comparing(AccessObject::getName));

        else if (sortMethod == SortMethod.LADDER_FIRST)
            stream = stream.sorted((access1, access2) -> {
                if (access1.isInstanceOf(SLadderRank.class) && access2.isInstanceOf(SLadderRank.class))
                    return 0;

                else if (access1.isInstanceOf(SLadderRank.class))
                    return -1;

                return 1;
            });

        else if (sortMethod == SortMethod.SPECIAL_FIRST)
            stream = stream.sorted((access1, access2) -> {
                if (access1.isInstanceOf(SSpecialRank.class) && access2.isInstanceOf(SSpecialRank.class))
                    return 0;

                else if (access1.isInstanceOf(SSpecialRank.class))
                    return -1;

                return 1;
            });

        else if (sortMethod == SortMethod.INPUT) {
            if (input == null) {
                sortMethod = SortMethod.ABC;
                return sorted(stream);

            } else {
                stream = stream.sorted((access1, access2) -> {
                    if (access1.getName().toLowerCase().startsWith(input) && access2.getName().toLowerCase().startsWith(input))
                        return 0;

                    else if (access1.getName().startsWith(input))
                        return -1;

                    return 1;
                });
            }
        } else if (sortMethod == SortMethod.PRESTIGE_FIRST) {
            stream = stream.sorted((access1, access2) -> {
                if (access1.isInstanceOf(Prestige.class) && access2.isInstanceOf(Prestige.class))
                    return 0;

                else if (access1.isInstanceOf(Prestige.class))
                    return -1;

                return 1;
            });
        }

        return stream.collect(Collectors.toList());
    }
}
