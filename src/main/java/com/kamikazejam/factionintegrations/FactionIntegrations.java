package com.kamikazejam.factionintegrations;

import com.kamikazejam.factionintegrations.integrations.*;
import com.kamikazejam.factionintegrations.utils.NmsManager;
import com.kamikazejam.factionintegrations.utils.PluginSource;
import com.kamikazejam.kamicommon.util.StringUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// ----------------------------------------------------------------------------------------------- //
// FactionIntegrations is meant to be a singleton-like class where all access goes through
//  getIntegration or getEconomy (where instances are created the first time they are needed)
// ----------------------------------------------------------------------------------------------- //

@SuppressWarnings("unused")
public class FactionIntegrations {
    // ----------------------------------------------------------------------------------------------- //
    //                                          STATIC FIELDS                                          //
    // ----------------------------------------------------------------------------------------------- //
    private static @Nullable JavaPlugin plugin;
    private static KFaction integration;
    private static Economy economy;

    // ----------------------------------------------------------------------------------------------- //
    //                                           API METHODS                                           //
    // ----------------------------------------------------------------------------------------------- //
    public static boolean hasFactionsInstalled() {
        return Bukkit.getPluginManager().getPlugin("Factions") != null;
    }
    public static void supplyPlugin(@Nullable JavaPlugin plugin) {
        if (FactionIntegrations.plugin == null) {
            FactionIntegrations.plugin = plugin;
        }
    }

    public static @NotNull KFaction getIntegration() {
        // Return the integration if it's already been setup
        if (integration != null) { return integration; }

        // Require a plugin to create an integration
        if (plugin == null) {
            throw new IllegalStateException("FactionIntegrations plugin not setup!");
        }

        // Throw if we can't set up the integration
        if (!createIntegration(plugin) || integration == null) {
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new IllegalStateException("Failed to setup integration!");
        }
        return integration;
    }
    public static @NotNull KFaction getIntegration(@NotNull JavaPlugin plugin) {
        FactionIntegrations.supplyPlugin(plugin);
        return getIntegration();
    }
    public static Economy getEconomy() {
        // Return the economy if it's already been setup
        if (economy != null) { return economy; }
        // Require a plugin to create an economy
        if (plugin == null) {
            throw new IllegalStateException("FactionIntegrations plugin not setup!");
        }
        // Throw if we can't set up the economy
        if (!setupEconomy(plugin) || economy == null) {
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new IllegalStateException("Failed to setup Economy!");
        }
        return economy;
    }
    public static @NotNull Economy getEconomy(@NotNull JavaPlugin plugin) {
        FactionIntegrations.supplyPlugin(plugin);
        return getEconomy();
    }






    // ----------------------------------------------------------------------------------------------- //
    //                                         PRIVATE HELPERS                                         //
    // ----------------------------------------------------------------------------------------------- //
    private static boolean createIntegration(JavaPlugin plugin) {
        // Store plugin instance
        FactionIntegrations.plugin = plugin;

        // Load Economy (required by many integrations)
        if (!setupEconomy(plugin)) {
            plugin.getLogger().info("Failed to setup Economy Integration");
            return false;
        }

        // Store Economy
        PluginSource.set(plugin, economy);

        // Try to setup Factions
        try {
            if (!setupFactions(plugin)) {
                plugin.getLogger().info("Failed to setup Factions Integration");
                return false;
            }

            PluginSource.setIntegration(integration);
            Bukkit.getPluginManager().registerEvents(FactionIntegrations.integration, plugin);
            return true;

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(String.format("[%s] - Disabled due to error during setup of integration!", plugin.getName()));
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean setupEconomy(JavaPlugin plugin) {
        if (FactionIntegrations.economy != null) { return true; }
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) { return false; }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) { return false; }

        FactionIntegrations.economy = rsp.getProvider();
        return FactionIntegrations.economy != null;
    }

    private static boolean setupFactions(JavaPlugin plugin) throws Exception {
        final Plugin uuidTest = Bukkit.getPluginManager().getPlugin("Factions");
        if (uuidTest == null) {
            return false;
        }

        // This is MC 1.20 Factions
        if (uuidTest.getDescription().getAuthors().contains("i01") && NmsManager.isAtOrAfter(18)) {
            FactionIntegrations.integration = new Factions1_20Integration(plugin);
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsX!");
            return true;
        }

        // This is Atlas Factions
        if (uuidTest.getDescription().getAuthors().contains("Elapsed")) {
            FactionIntegrations.integration = new AtlasIntegration();
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into AtlasFactions!");
            return true;
        }

        // This is Joseph's Factions
        if (uuidTest.getDescription().getAuthors().contains("Cayorion")) {
            FactionIntegrations.integration = new MCoreIntegration();
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into Joseph MassiveCore Factions!");
            return true;
        }

        // This is StellarFactions
        if (uuidTest.getDescription().getAuthors().contains("ipodtouch0218")) {
            FactionIntegrations.integration  = new StellarIntegration();
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into StellarFactions!");
            return true;
        }

        // This is SaberFactionsX
        if (uuidTest.getDescription().getAuthors().contains("DroppingAnvil")) {
            if (uuidTest.getDescription().getVersion().equals("1.6.9.5-2.0.6-X")) {
                FactionIntegrations.integration = new SaberFactionsXIntegration();
                send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into SaberFactionsX!");
                return true;
            }
            FactionIntegrations.integration = new SaberFactionsIntegration();
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into SaberFactions!");
            return true;
        }

        // This is Jerry's VulcanFactions
        if (uuidTest.getDescription().getAuthors().contains("CmdrKittens") && uuidTest.getDescription().getAuthors().contains("Jerry")) {
            FactionIntegrations.integration = new JerryIntegration();
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsUUID (Jerry's Fork)!");
            return true;
        }

        // This is Factions UUID (new)
        if (uuidTest.getDescription().getAuthors().contains("mbaxter")) {
            FactionIntegrations.integration = new NewUUIDIntegration();
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsUUID!");
            return true;
        }

        // This is FactionsUUID.jar (written using Golfing's fork)
        if (uuidTest.getDescription().getAuthors().contains("CmdrKittens")) {
            FactionIntegrations.integration = new UUIDIntegration();
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsUUID!");
            return true;
        }

        // This is Factions-Jartex.jar
        if (uuidTest.getDescription().getAuthors().contains("JustThiemo")) {
            FactionIntegrations.integration = new JartexIntegration();
            send("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsUUID (Jartex)!");
            return true;
        }

        // LockedThread Factions removed by Golfing
        // SavageFactions was abandoned (is now FactionsX)
        // SupremeFactions is now GucciFactions (dropping support for SupremeFac)
        // UltimateFactionsV2 can't be supported due to poor API
        // TODO: GucciFactions integration?
        // TODO: DominionFactions had special logic, but we need the jar to re-add

        return false;
    }

    private static void send(String s) {
        if (plugin == null) {
            Bukkit.getConsoleSender().sendMessage(StringUtil.t(s));
        }else {
            plugin.getLogger().info(StringUtil.t(s));
        }
    }
}
