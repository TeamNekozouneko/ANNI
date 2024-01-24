package net.nekozouneko.anni.util;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static double bossBarProgress(double max, double val) {
        Preconditions.checkArgument(max >= 0, "max is negative value");
        double prg = val / max;

        if (prg > 1) prg = 1;
        if (prg < 0) prg = 0;

        return prg;
    }

    public static void giveOrDrop(Player player, Collection<ItemStack> items) {
        ItemStack[] array = new ItemStack[items.size()];
        int i = 0;
        for (ItemStack item : items) {
            array[i] = item;
            i++;
        }

        giveOrDrop(player, array);
    }

    public static void giveOrDrop(Player player, ItemStack... items) {
        player.getInventory().addItem(items).values().forEach(item ->
            player.getWorld().dropItemNaturally(player.getLocation(), item)
        );
    }

    public static char numberToChar( int i) {
        return (char) (i + 0x30);
    }

    public static int count(String s, String regex) {
        Matcher m = Pattern.compile(regex).matcher(s);
        int i = 0;
        while (m.find()) i++;

        return i;
    }

    public static boolean canDamage(Player from, Player target) {
        if (from.equals(target)) return true;
        if (getJoinedTeam(from) != null) {
            Team ft = getJoinedTeam(from);
            if (ft.equals(getJoinedTeam(target)) && !ft.allowFriendlyFire()) {
                return false;
            }
        }

        return true;
    }

    public static boolean classExists(String class1) {
        try {
            Class.forName(class1);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

}
