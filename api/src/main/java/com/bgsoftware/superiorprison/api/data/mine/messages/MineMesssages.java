package com.bgsoftware.superiorprison.api.data.mine.messages;

import java.util.Optional;
import java.util.Set;

public interface MineMesssages {
    Optional<MineMessage> get(int id);

    Set<MineMessage> get();

    MineChatMessage addChatMessage(String content, long every);

    MineTitleMessage addTitleMessage(int fadeIn, int stay, int fadeOut, String title, String subTitle, long every);

    MineActionBarMessage addActionBarMessage(String content, long every);
}
