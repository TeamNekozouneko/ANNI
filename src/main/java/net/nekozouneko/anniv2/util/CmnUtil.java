package net.nekozouneko.anniv2.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class CmnUtil {

    private CmnUtil() {}

    public static String replaceColorCode(String s) {
        return s
                .replaceAll("&#([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])", "§x§$1§$2§$3§$4§$5§$6")
                .replaceAll("&#([0-9A-F])([0-9A-F])([0-9A-F])", "§x§$1§$2§$3")
                .replaceAll("&([0-9A-FK-ORXa-fk-orx])", "§$1");
    }

    public static <T> T arrayGetOrNull(T[] arr, int pos) {
        if (arr.length > pos) return arr[pos];
        return null;
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

}
