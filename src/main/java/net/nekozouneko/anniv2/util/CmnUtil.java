package net.nekozouneko.anniv2.util;

import com.google.common.base.Preconditions;
import net.nekozouneko.commons.lang.collect.Collections3;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CmnUtil {

    private CmnUtil() {}

    public static String replaceColorCode(String s) {
        return s
                .replaceAll("&#([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])", "§x§$1§$2§$3§$4§$5§$6")
                .replaceAll("&#([0-9A-F])([0-9A-F])([0-9A-F])", "§x§$1§$2§$3")
                .replaceAll("&([0-9A-FK-ORXa-fk-orx])", "§$1");
    }

    public static long toTick(double l) {
        return (long) (l * 20D);
    }

    public static double toSecond(long l) {
        return l / 20D;
    }

    /**
     * MM...:SS
     * @param second Total second
     * @return Timer (MM...:SS)
     */
    public static String secminTimer(long second) {
        long m = second / 60;
        long s = second - (m * 60);

        return String.format("%02d", m) + ":" + String.format("%02d", s);
    }

    public static Team getJoinedTeam(Player player) {
        return player.getScoreboard().getPlayerTeam(player);
    }

    public static void assignAtEquality(Set<Player> players, Set<Team> teams, boolean allowReplaceCurrTeam) {
        Preconditions.checkArgument(!players.isEmpty(), "Player Set is empty");
        Preconditions.checkArgument(!teams.isEmpty(), "Teams Set is empty");

        players.stream() // もしplayerがチームに入ってないもしくはteamsにないチームに参加しているならtrue (allowReplaceCurrTeamがtrueなら全部許可)
                .filter(player -> {
                    Team t = getJoinedTeam(player);
                    return (t == null || !teams.contains(t)) || allowReplaceCurrTeam;
                })
                .forEach(player -> {
                    Map<Team, Integer> teamSize = new HashMap<>();
                    teams.forEach(team -> teamSize.put(team, team.getSize()));

                    if ( // 全部の値が一緒なら
                            Collections3.allValueEquals(
                                    teamSize.values(),
                                    teamSize.values().iterator().next() // チーム人数リストの最初の要素
                            )
                    ) teams.iterator().next().addPlayer(player); // 最初の要素 (チーム)に参加させる
                    else { // 一緒じゃなければ均等に分散させる
                        Map.Entry<Team, Integer> minTeam = null; // 人数が少ないチーム

                        for (Map.Entry<Team, Integer> entry : teamSize.entrySet()) {
                            if (minTeam == null || minTeam.getValue() > entry.getValue())
                                minTeam = entry; // minTeamがnullもしくはminTeamの人数より少なければentryに置き換える
                        }

                        minTeam.getKey().addPlayer(player);
                    }
                });
    }

    public static double bossBarProgress(double max, double val) {
        Preconditions.checkArgument(max <= 0, "max is zero or negative value");
        double prg = val / max;

        if (prg > 1) prg = 1;
        if (prg < 0) prg = 0;

        return prg;
    }

}
