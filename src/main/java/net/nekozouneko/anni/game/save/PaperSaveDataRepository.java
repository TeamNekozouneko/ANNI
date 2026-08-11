package net.nekozouneko.anni.game.save;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.game.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class PaperSaveDataRepository implements SaveDataRepository {

    public record SaveData(boolean saveOnlyTeamColor, ANNITeam color, ItemStack[] inventory, ItemStack[] enderChest, Location lastLocation, double health, int level, float experience) {}

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
    public SavedResult load(UUID player) {
        var playerData = data.get(player);
        var bukkitPlayer = Bukkit.getPlayer(player);

        Preconditions.checkArgument(bukkitPlayer != null);

        teamManager.join(playerData.color(), player);

        if (!playerData.saveOnlyTeamColor()) {
            bukkitPlayer.getInventory().setContents(playerData.inventory());
            bukkitPlayer.getEnderChest().setContents(playerData.enderChest());
            if (playerData.lastLocation != null)
                bukkitPlayer.teleportAsync(playerData.lastLocation());
            bukkitPlayer.setHealth(playerData.health());
            bukkitPlayer.setLevel(playerData.level());
            bukkitPlayer.setExp(playerData.experience());

            return new SavedResult(playerData.lastLocation() != null, true);
        }

        return new SavedResult(false, false);
    }

    @Override
    public boolean canLoad(UUID player) {
        return data.containsKey(player);
    }

    @Override
    public void save(UUID player, boolean saveOnlyTeamColor, boolean resetPosition) {
        var bukkitPlayer = Bukkit.getPlayer(player);

        Preconditions.checkArgument(bukkitPlayer != null);

        data.put(player, new SaveData(
                saveOnlyTeamColor,
                teamManager.getTeamColorByPlayer(player),
                saveOnlyTeamColor ? null : bukkitPlayer.getInventory().getContents().clone(),
                saveOnlyTeamColor ? null : bukkitPlayer.getEnderChest().getContents().clone(),
                saveOnlyTeamColor || resetPosition ? null : bukkitPlayer.getLocation().clone(),
                saveOnlyTeamColor ? 0 : bukkitPlayer.getHealth(),
                saveOnlyTeamColor ? 0 : bukkitPlayer.getLevel(),
                saveOnlyTeamColor ? 0 : bukkitPlayer.getExp()
        ));
    }
}
