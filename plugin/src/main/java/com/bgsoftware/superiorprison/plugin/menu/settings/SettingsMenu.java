package com.bgsoftware.superiorprison.plugin.menu.settings;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.input.PlayerInput;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class SettingsMenu extends OPagedMenu<SettingsObject> implements OMenu.Templateable {

    private final SNormalMine mine;

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

                    Runnable onCancel = () -> {
                        previousMove = true;
                        refresh();
                    };

                    new PlayerInput<>((Player) event.getWhoClicked())
                            .parser(string -> {
                                try {
                                    return settingsObject.mapper().accept(string);
                                } catch (Throwable throwable) {
                                    throw new IllegalStateException(throwable.getMessage());
                                }
                            })
                            .timeOut(TimeUnit.MINUTES, 2)
                            .onInput((input, object) -> {
                                settingsObject.currentValue(object);
                                settingsObject.onComplete().accept(object);

                                messageBuilder(settingsObject.completeMessage())
                                        .replace(viewer, mine, mine.getSettings(), settingsObject)
                                        .send(event.getWhoClicked());

                                input.cancel();
                                onCancel.run();
                            })
                            .onCancel(onCancel)
                            .onError((input, err) -> messageBuilder(LocaleEnum.EDIT_SETTINGS_ERROR.getWithErrorPrefix())
                                    .replace("{setting_name}", settingsObject.id())
                                    .replace("{error}", err.getMessage())
                                    .send(input.player()))
                            .listen();
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
