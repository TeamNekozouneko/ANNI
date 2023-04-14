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

    public static String getFormattedDateNow(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        return sdf.format(Calendar.getInstance().getTime());
    }

}
