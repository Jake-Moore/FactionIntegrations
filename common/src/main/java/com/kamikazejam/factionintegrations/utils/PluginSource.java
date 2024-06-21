package com.kamikazejam.factionintegrations.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginSource {
    private static @Nullable JavaPlugin plugin;
    private static Economy economy;

    public static @NotNull JavaPlugin get() {
        if (plugin == null) {
            throw new IllegalStateException("Plugin not set");
        }
        return plugin;
    }

    public static void set(@NotNull JavaPlugin plugin, @NotNull Economy economy) {
        if (PluginSource.plugin != null && PluginSource.plugin.isEnabled()) {
            return;
        }
        PluginSource.plugin = plugin;
        PluginSource.economy = economy;
    }

    public static Economy getEconomy() {
        if (economy == null) {
            throw new IllegalStateException("Economy not set");
        }
        return economy;
    }
}
