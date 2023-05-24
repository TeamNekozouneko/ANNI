package net.nekozouneko.anniv2.arena.team;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.annotations.SerializedName;
import net.md_5.bungee.api.ChatColor;
import net.nekozouneko.anniv2.ANNIPlugin;

import java.lang.reflect.Field;
import java.util.Map;

public enum ANNITeam {

    @SerializedName("red")
    RED("team.red.display", "team.red.prefix", ChatColor.RED, 'r'),
    @SerializedName("blue")
    BLUE("team.blue.display", "team.blue.prefix", ChatColor.BLUE, 'b'),
    @SerializedName("green")
    GREEN("team.green.display", "team.green.prefix", ChatColor.GREEN, 'g'),
    @SerializedName("yellow")
    YELLOW("team.yellow.display", "team.yellow.prefix", ChatColor.YELLOW, 'y');

    private final String name;
    private final String prefix;
    private final ChatColor cc;
    private final char bigChar;

    private ANNITeam(String name, String prefix, ChatColor cc, char bigChar) {
        this.name = name;
        this.prefix = prefix;
        this.cc = cc;
        this.bigChar = bigChar;
    }

    public String getTeamName() {
        return ANNIPlugin.getInstance().getMessageManager().build(name);
    }

    public String getTeamPrefix() {
        return ANNIPlugin.getInstance().getMessageManager().build(prefix);
    }

    public ChatColor getColorCode() {
        return cc;
    }

    public String getColoredName() {
        return cc + getTeamName();
    }

    @SuppressWarnings("unchecked")
    public char getCCChar() {
        IllegalAccessException iae = null;
        try {
            Field cha = cc.getClass().getDeclaredField("BY_CHAR");
            try {
                cha.setAccessible(true);
                BiMap<Character, ChatColor> bm = HashBiMap.create((Map<Character, ChatColor>) cha.get(null));
                cha.setAccessible(false);

                return bm.inverse().get(cc);
            }
            catch (IllegalAccessException e) {
                iae = e;
            }
            finally {
                cha.setAccessible(false);
            }
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException(iae);
    }

    public char getBigChar() {
        return bigChar;
    }

}
