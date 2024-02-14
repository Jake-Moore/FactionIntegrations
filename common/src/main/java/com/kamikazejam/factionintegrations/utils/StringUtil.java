package com.kamikazejam.factionintegrations.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    @SuppressWarnings("all")
    public static final char COLOR_CHAR = '\u00A7';

    public static String t(String msg) {
        String s = translateAlternateColorCodes(msg);

        // For 1.16+ translate hex color codes as well
        if (!supportsHexCodes()) return s;

        Pattern hex = Pattern.compile("&(#[A-Fa-f0-9]{6})");
        Matcher matcher = hex.matcher(s);
        while (matcher.find()) {
            StringBuilder s2 = new StringBuilder(COLOR_CHAR + "x");
            for (char c : matcher.group(1).substring(1).toCharArray()) {
                s2.append(COLOR_CHAR).append(c);
            }

            s = s.replace(matcher.group(), "" + s2);
        }
        return s;
    }

    public static boolean supportsHexCodes() {
        return NmsManager.isAtOrAfter(16);
    }

    private static String translateAlternateColorCodes(String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }
}
