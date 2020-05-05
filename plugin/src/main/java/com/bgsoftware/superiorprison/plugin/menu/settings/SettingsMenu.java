package com.bgsoftware.superiorprison.plugin.menu.settings;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.message.OMessage;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class SettingsMenu extends OPagedMenu<SettingsObject> implements OMenu.Templateable {

    private SNormalMine mine;
    public SettingsMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineSettings", viewer);
        this.mine = mine;

        clickHandler("setting")
                .handle(event -> {
                    SettingsObject settingsObject = requestObject(event.getRawSlot());
                    previousMove = false;
                    event.getWhoClicked().closeInventory();

                    messageBuilder(settingsObject.requestMessage())
                            .replace(viewer, mine, mine.getSettings(), settingsObject)
                            .send(event.getWhoClicked());

                    AtomicReference<Object> mappedObject = new AtomicReference<>(null);
                    SubscriptionFactory.getInstance().subscribeTo(AsyncPlayerChatEvent.class, chatEvent -> {
                        chatEvent.setCancelled(true);
                        
                        Object o = mappedObject.get();
                        if (o == null) return;

                        settingsObject.currentValue(o);
                        settingsObject.onComplete().accept(o);

                        messageBuilder(settingsObject.completeMessage())
                                .replace(viewer, mine, mine.getSettings(), settingsObject)
                                .send(event.getWhoClicked());
                    }, new SubscriptionProperties<AsyncPlayerChatEvent>().filter(chatEvent -> {
                        try {
                            Object accept = settingsObject.mapper().accept(chatEvent.getMessage());
                            mappedObject.set(accept);
                            return true;
                        } catch (Throwable throwable) {
                            messageBuilder(LocaleEnum.EDIT_SETTINGS_ERROR.getWithErrorPrefix())
                                    .replace("{setting_name}", settingsObject.id())
                                    .replace("{error}", throwable.getMessage())
                                    .send(chatEvent);
                            return false;
                        }
                    }).timesToRun(1));
                });
    }

    @Override
    public List<SettingsObject> requestObjects() {
        return mine.getSettings().getSettingObjects();
    }

    @Override
    public OMenuButton toButton(SettingsObject obj) {
        Optional<OMenuButton> optionalSetting = getTemplateButtonFromTemplate("setting");
        if (!optionalSetting.isPresent()) return null;

        OMenuButton settingButton = optionalSetting.get().clone();

        String settingId = obj.id().replaceAll("\\s+", "-");
        OMenuButton.ButtonItemBuilder state = settingButton.getStateItem(settingId);
        if (state == null)
            state = settingButton.getDefaultStateItem();

        settingButton.currentItem(state.getItemStackWithPlaceholders(obj));
        return settingButton;
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{mine, mine.getSettings()};
    }
}
