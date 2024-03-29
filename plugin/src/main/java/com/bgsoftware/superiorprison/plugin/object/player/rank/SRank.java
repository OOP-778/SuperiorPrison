package com.bgsoftware.superiorprison.plugin.object.player.rank;

import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.object.player.Access;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class SRank implements Rank, Access {
  private @NonNull final String name;
  private @NonNull final String prefix;

  private final List<String> commands;

  private final List<String> permissions;
}
