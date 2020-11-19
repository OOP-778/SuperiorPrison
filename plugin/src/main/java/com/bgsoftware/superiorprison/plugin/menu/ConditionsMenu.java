package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.plugin.condition.MineConditionTemplate;
import com.bgsoftware.superiorprison.plugin.condition.MineConditionTemplates;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.access.MineCondition;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.input.PlayerInput;
import com.bgsoftware.superiorprison.plugin.util.input.multi.MultiPlayerInput;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.message.impl.OChatMessage;

import java.util.*;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;

public class ConditionsMenu extends OPagedMenu<MineCondition> implements OMenu.Templateable {
    private SNormalMine mine;

    public ConditionsMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineAccessList", viewer);

        clickHandler("create")
                .handle(event -> {
                    forceClose();
                    LocaleEnum
                            .MINE_CONDITION_CREATE_STEP1
                            .getWithPrefix()
                            .send(event.getWhoClicked());

                    new PlayerInput<String>(getViewer().getPlayer())
                            .parser(in -> in)
                            .onInput((pi, input) -> {
                                if (input.equalsIgnoreCase("template")) {
                                    createFromTemplate(pi);
                                }
                            })
                            .onCancel(this::refresh)
                            .listen();
                });
        this.mine = mine;
    }

    private void createFromTemplate(PlayerInput<String> playerInput) {
        listedBuilder(MineConditionTemplate.class)
                .message(LocaleEnum.MINE_CONDITION_CREATE_FROM_TEMPLATE_TYPE.getWithPrefix())
                .identifier("{TEMPLATE}")
                .addObject(MineConditionTemplates.getTemplateMap().values().toArray(new MineConditionTemplate[0]))
                .objectContentModifier((content, object) -> {
                    content.replace("{template_name}", object.name());
                    content
                            .hover()
                            .add(object.description().toArray(new String[0]));
                })
                .send(getViewer().getPlayer());

        playerInput.onInput((pi, input) -> {
            MineConditionTemplate template = MineConditionTemplates.getTemplateMap().get(input);
            if (template == null)
                throw new IllegalStateException("Failed to find template by name: " + input);
            pi.cancel();

            MultiPlayerInput multiPlayerInput = new MultiPlayerInput(pi.player());
            template.parser().getValues().forEach((pair, parser) -> {
                new MultiPlayerInput.InputData()
                        .id(pair.getFirst())
                        .requestMessage(new OChatMessage(pair.getSecond()))
                        .parser(in -> {
                            String parse = parser.parse((String) in);
                            return parse;
                        });
            });

            multiPlayerInput.onInput((p, vars) ->{
                String apply = template.parser().getTemplateParser().apply((Map<String, String>) vars);
                System.out.println(apply);
            });
        });
    }

    @Override
    public List<MineCondition> requestObjects() {
        return new ArrayList<>(mine.getAccess().getConditions());
    }

    @Override
    public OMenuButton toButton(MineCondition obj) {
        Optional<OMenuButton> condition = getTemplateButtonFromTemplate("condition");
        if (!condition.isPresent()) return null;

        OMenuButton conditionButton = condition.get().clone();
        OMenuButton.ButtonItemBuilder defaultStateItem = conditionButton.getDefaultStateItem();

        return conditionButton.currentItem(new OItem(defaultStateItem.getItemStackWithPlaceholders(obj)).getItemStack());
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{mine};
    }
}
