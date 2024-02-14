package com.kamikazejam.factionintegrations.utils;

import org.bukkit.Bukkit;

public class NmsManager {

    private static Integer minorVersion = null;

    public static boolean isAtOrAfter(int version) {
        return getMinorVersion() >= version;
    }

    private static int getMinorVersion() {
        if (minorVersion == null) {
            // package looks like net.minecraft.server.v1_16_R1
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            // version looks like 1_16_R1
            minorVersion = Integer.parseInt(version.split("_")[1]);
            // minorVersion is parsed to be 16
        }
        return minorVersion;
    }
}
