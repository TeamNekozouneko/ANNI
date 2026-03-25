package net.nekozouneko.anni.game.team;

import com.google.common.base.Preconditions;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.team.ANNITeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class PaperTeamRepository implements TeamRepository {

    private final Scoreboard scoreboard;

    private final Map<ANNITeam, Team> teams = new EnumMap<>(ANNITeam.class);

    public PaperTeamRepository(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public void create(ANNITeam color) {
        Preconditions.checkState(!teams.containsKey(color));

        Team team = scoreboard.registerNewTeam(color.getId());

        team.displayName(ANNIPlugin.getInstance().getTranslationManager().component(color.getNameKey()));
        team.prefix(ANNIPlugin.getInstance().getTranslationManager().component(color.getPrefix()));
        team.color(color.getColor());
        team.setAllowFriendlyFire(false);
        team.setCanSeeFriendlyInvisibles(true);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);

        teams.put(color, team);
    }

    public void remove(ANNITeam color) {
        Team team = teams.remove(color);
        if (team != null) team.unregister();
    }

    public void clear() {
        teams.keySet().forEach(this::clear);
    }

    public void clear(ANNITeam color) {
        Team team = teams.get(color);

        if (team == null) return;
        team.removeEntries(new HashSet<>(team.getEntries()));
    }

    @Override
    public void join(ANNITeam color, UUID player) {
        Player bukkitPlayer = Bukkit.getPlayer(player);
        Preconditions.checkArgument(bukkitPlayer != null);

        teams.get(color).addEntity(bukkitPlayer);
    }

    @Override
    public void leave(UUID player) {
        Player bukkitPlayer = Bukkit.getPlayer(player);
        Preconditions.checkArgument(bukkitPlayer != null);

        teams.values().forEach(team -> team.removeEntity(bukkitPlayer));
    }
}
