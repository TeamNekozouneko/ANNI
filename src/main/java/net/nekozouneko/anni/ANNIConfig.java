package net.nekozouneko.anni;

import com.google.common.base.Preconditions;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;

public class ANNIConfig {

    private static FileConfiguration conf;

    static void setConfig(FileConfiguration conf) {
        ANNIConfig.conf = conf;
    }

    public static boolean isTeamEnabled(ANNITeam at) {
        Preconditions.checkArgument(at != null);

        StringBuilder sb = new StringBuilder("default-arena.enabled-teams.");

        switch (at) {
            case RED:
                sb.append("red");
                break;
            case BLUE:
                sb.append("blue");
                break;
            case GREEN:
                sb.append("green");
                break;
            case YELLOW:
                sb.append("yellow");
                break;
        }

        return conf.getBoolean(sb.toString());
    }

    public static int getTeamMinPlayers() {
        return conf.getInt("default-arena.team-min-players");
    }

    public static int getTeamMaxPlayers() {
        return conf.getInt("default-arena.team-max-players");
    }

    public static int getDefaultHealth() {
        return conf.getInt("default-arena.default-health");
    }

    public static boolean isVotifierVoteEnabled() {
        return conf.getBoolean("on-votifier.enabled");
    }

    public static double getVotePoints() {
        return conf.getDouble("on-votifier.points");
    }

    public static boolean isEnabledCustomKits() {
        return conf.getBoolean("kits.enable-custom-kit");
    }

    public static boolean isCustomKitOnly() {
        return conf.getBoolean("kits.custom-kit-only");
    }

    public static Locale getDefaultLocale() {
        return CmnUtil.localeCodeToLocale(conf.getString("languages.default", "ja_JP"));
    }

}
