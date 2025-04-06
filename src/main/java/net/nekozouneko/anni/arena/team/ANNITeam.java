package net.nekozouneko.anni.arena.team;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.nekozouneko.anni.ANNIPlugin;

import java.lang.reflect.Field;
import java.util.Map;

@AllArgsConstructor
public enum ANNITeam {

    @SerializedName("RED")
    RED("team.red.display", "team.red.prefix", "team.red.name", ChatColor.RED, 'r'),
    @SerializedName("BLUE")
    BLUE("team.blue.display", "team.blue.prefix", "team.blue.name", ChatColor.BLUE, 'b'),
    @SerializedName("GREEN")
    GREEN("team.green.display", "team.green.prefix", "team.green.name", ChatColor.GREEN, 'g'),
    @SerializedName("YELLOW")
    YELLOW("team.yellow.display", "team.yellow.prefix", "team.yellow.name", ChatColor.YELLOW, 'y');

    private final String name;
    private final String prefix;
    @Getter
    private final String nameKey;
    private final ChatColor cc;
    @Getter
    private final char bigChar;


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

}
