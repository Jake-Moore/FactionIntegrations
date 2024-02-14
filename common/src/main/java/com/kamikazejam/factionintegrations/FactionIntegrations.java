package com.kamikazejam.factionintegrations;

import com.kamikazejam.factionintegrations.integrations.KFaction;
import com.kamikazejam.kamicommon.nms.NmsManager;
import com.kamikazejam.kamicommon.util.StringUtil;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unused")
public class FactionIntegrations {
    @Getter
    private static JavaPlugin plugin;
    @Getter
    private static KFaction integration;
    @Getter
    private static Economy economy;

    public static void setup(JavaPlugin plugin) {
        FactionIntegrations.plugin = plugin;
        try {
            if (!setupFactions(plugin)) {
                plugin.getLogger().info("Failed to setup Factions Integration");
                plugin.getPluginLoader().disablePlugin(plugin);
            }

            // Yoink all this shit from golfing :) ty
            try {
                plugin.getServer().getPluginManager().registerEvents(FactionIntegrations.integration, plugin);
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(String.format("[%s] - Disabled due to error during setup of integration!", plugin.getName()));
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                e.printStackTrace();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().info("Failed to setup Factions Integration");
        }

        if (!setupEconomy(plugin)) {
            plugin.getLogger().info("Failed to setup Economy Integration");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    public static JavaPlugin get() {
        return FactionIntegrations.getPlugin();
    }

    private static boolean setupEconomy(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        FactionIntegrations.economy = rsp.getProvider();
        return FactionIntegrations.economy != null;
    }

    private static boolean setupFactions(JavaPlugin plugin) {
        final Plugin uuidTest = Bukkit.getPluginManager().getPlugin("Factions");
        if (uuidTest == null) {
            return false;
        }

        // This is MC 1.20 Factions
        if (uuidTest.getDescription().getAuthors().contains("i01") && NmsManager.getFormattedNmsDouble() >= 1180) {
            FactionIntegrations.integration = createIntegration("Factions1_20Integration");
            Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsX!"));
            return true;
        }

        // This is Atlas Factions
        if (uuidTest.getDescription().getAuthors().contains("Elapsed")) {
            FactionIntegrations.integration = createIntegration("AtlasIntegration");
            plugin.getLogger().info(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into AtlasFactions!"));
            return true;
        }

        // This is Joseph's Factions
        if (uuidTest.getDescription().getAuthors().contains("Cayorion")) {
            FactionIntegrations.integration = createIntegration("MCoreIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into Joseph MassiveCore Factions!"));
            return true;
        }

        // This is StellarFactions
        if (uuidTest.getDescription().getAuthors().contains("ipodtouch0218")) {
            FactionIntegrations.integration = createIntegration("StellarIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into StellarFactions!"));
            return true;
        }

        // This is SaberFactionsX
        if (uuidTest.getDescription().getAuthors().contains("DroppingAnvil")) {
            if (uuidTest.getDescription().getVersion().equals("1.6.9.5-2.0.6-X")) {
                FactionIntegrations.integration = createIntegration("SaberFactionsXIntegration");
                Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into SaberFactionsX!"));
                return true;
            }
            FactionIntegrations.integration = createIntegration("SaberFactionsIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into SaberFactions!"));
            return true;
        }

        // This is Jerry's VulcanFactions
        if (uuidTest.getDescription().getAuthors().contains("CmdrKittens") && uuidTest.getDescription().getAuthors().contains("Jerry")) {
            FactionIntegrations.integration = createIntegration("JerryIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsUUID (Jerry's Fork)!"));
            return true;
        }

        // This is Factions UUID (new)
        if (uuidTest.getDescription().getAuthors().contains("mbaxter")) {
            FactionIntegrations.integration = createIntegration("NewUUIDIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsUUID!"));
            return true;
        }

        // This is FactionsUUID.jar (written using Golfing's fork)
        if (uuidTest.getDescription().getAuthors().contains("CmdrKittens")) {
            FactionIntegrations.integration = createIntegration("UUIDIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsUUID!"));
            return true;
        }

        // This is Factions-Jartex.jar
        if (uuidTest.getDescription().getAuthors().contains("JustThiemo")) {
            FactionIntegrations.integration = createIntegration("JartexIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&l" + plugin.getName() + " &7- &aSuccessfully hooked into FactionsUUID (Jartex)!"));
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

    private static KFaction createIntegration(String name) {
        try {
            String currPackage = FactionIntegrations.class.getPackage().getName();
            String target = currPackage + ".integrations." + name;
            return (KFaction) Class.forName(target).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException("Failed to create integration '" + name + "'", e);
        }
    }

    private static String c(String s) {
        return StringUtil.t(s);
    }
}
