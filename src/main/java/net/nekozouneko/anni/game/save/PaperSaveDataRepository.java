package net.nekozouneko.anni.game.save;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.game.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class PaperSaveDataRepository implements SaveDataRepository {

    public record SaveData(ANNITeam color, ItemStack[] inventory, double health, int level, float experience) {}

    private final Map<UUID, SaveData> data = new HashMap<>();
    private final TeamManager teamManager;

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public void remove(UUID player) {
        data.remove(player);
    }

    @Override
    public void load(UUID player) {
        var playerData = data.get(player);
        var bukkitPlayer = Bukkit.getPlayer(player);

        Preconditions.checkArgument(bukkitPlayer != null);

        teamManager.join(playerData.color(), player);
        bukkitPlayer.getInventory().setContents(playerData.inventory());
        bukkitPlayer.setHealth(playerData.health());
        bukkitPlayer.setLevel(playerData.level());
        bukkitPlayer.setExp(playerData.experience());
    }

    @Override
    public boolean canLoad(UUID player) {
        return data.containsKey(player);
    }

    @Override
    public void save(UUID player) {
        var bukkitPlayer = Bukkit.getPlayer(player);

        Preconditions.checkArgument(bukkitPlayer != null);

        data.put(player, new SaveData(
                teamManager.getTeamColorByPlayer(player),
                bukkitPlayer.getInventory().getContents().clone(),
                bukkitPlayer.getHealth(),
                bukkitPlayer.getLevel(),
                bukkitPlayer.getExp()
        ));
    }
}
