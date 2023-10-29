package net.nekozouneko.anni;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import net.nekozouneko.anni.arena.team.ANNITeam;
import org.bukkit.configuration.file.FileConfiguration;

public class ANNIConfig {

    public enum DatabaseType {
        SQLITE,
        MYSQL,
        MARIADB
    }

    private static FileConfiguration conf;

    static void setConfig(FileConfiguration conf) {
        ANNIConfig.conf = conf;
    }

    public static DatabaseType getDatabaseType() {
        return Enums.getIfPresent(
                DatabaseType.class,
                conf.getString("database.type", "sqlite").toUpperCase()
        ).or(DatabaseType.SQLITE);
    }

    public static String getDBTablePrefix() {
        return conf.getString("database.prefix", "anniv2_");
    }

    public static String getLocalDBPath() {
        return ANNIPlugin.getInstance().getDataFolder() + "/" + conf.getString("database.local.path", "anni.db");
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

}
