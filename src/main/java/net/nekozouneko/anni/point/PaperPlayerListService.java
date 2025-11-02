package net.nekozouneko.anni.point;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;

import java.util.UUID;

@RequiredArgsConstructor
public class PaperPlayerListService implements PlayerListService {

    private final LevelManager levelManager;

    @Override
    public void reset(UUID player) {
        var bukkitPlayer = Bukkit.getPlayer(player);

        bukkitPlayer.playerListName(null);
    }

    @Override
    public void showPlayerLevels(UUID player) {
        var bukkitPlayer = Bukkit.getPlayer(player);

        bukkitPlayer.playerListName(
                Component.text()
                        .append(Component.text(levelManager.getLevel(bukkitPlayer)).decorate(TextDecoration.BOLD))
                        .append(Component.space())
                        .append(bukkitPlayer.displayName())
                        .asComponent()
        );
    }
}
