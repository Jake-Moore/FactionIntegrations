package com.kamikazejamplugins.factionintegrations;

import com.kamikazejamplugins.factionintegrations.integrations.*;
import com.kamikazejamplugins.factionintegrations.integrations.interfaces.KFaction;
import com.kamikazejamplugins.kamicommon.util.StringUtil;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unused")
public class FactionIntegrations {
    @Getter private static JavaPlugin plugin;
    @Getter private static KFaction integration;
    @Getter private static Economy economy;

    public static void setup(JavaPlugin plugin) {
        FactionIntegrations.plugin = plugin;
        try {
            if (!setupFactions()) {
                plugin.getLogger().info("Failed to setup Factions Integration");
                plugin.getPluginLoader().disablePlugin(plugin);
            }

            // Yoink all this shit from golfing :) ty
            try {
                plugin.getServer().getPluginManager().registerEvents(FactionIntegrations.integration, plugin);
            }
            catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(String.format("[%s] - Disabled due to error during setup of integration!", plugin.getName()));
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                e.printStackTrace();
                return;
            }
        }catch (Exception e) {
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

    private static boolean setupFactions() throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        final Plugin uuidTest = Bukkit.getPluginManager().getPlugin("Factions");
        if (uuidTest == null) { return false; }

        //TODO: Don't have the jar for this so I can't test it
        if (uuidTest.getDescription().getAuthors().contains("Elapsed")) {
            FactionIntegrations.integration = new AtlasIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into AtlasFactions!"));
            return true;
        }

        // This is Factions-Joseph.jar
        if (uuidTest.getDescription().getAuthors().contains("Cayorion")) {
            FactionIntegrations.integration = new MCoreIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into Joseph MassiveCore Factions!"));
            return true;
        }

        //TODO: Don't have the jar for this so I can't test it
        if (uuidTest.getDescription().getAuthors().contains("AL56AF50") || uuidTest.getDescription().getAuthors().contains("SupremeDev")) {
            FactionIntegrations.integration = new SupremeIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into SupremeFactions!"));
            return true;
        }

        //TODO: Don't have the jar for this so I can't test it
        if (uuidTest.getDescription().getAuthors().contains("ipodtouch0218")) {
            FactionIntegrations.integration = new StellarIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into StellarFactions!"));
            return true;
        }

        // This is Factions-Savage.jar
        if (uuidTest.getDescription().getMain().contains("SavageFactions")) {
            FactionIntegrations.integration = new SavageFactionsIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into SavageFactions!"));
            return true;
        }

        // This is SaberFactionsX.jar
        if (uuidTest.getDescription().getAuthors().contains("DroppingAnvil")) {
            if (uuidTest.getDescription().getVersion().equals("1.6.9.5-2.0.6-X")) {
                FactionIntegrations.integration = new SaberFactionsXIntegration();
                Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into SaberFactionsX!"));
                return true;
            }
            FactionIntegrations.integration = new SaberFactionsIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into SaberFactions!"));
            return true;
        }

        // TODO: Don't have the jar for this so I can't test it
        if (uuidTest.getDescription().getAuthors().contains("LockedThread")) {
            FactionIntegrations.integration = new LockedThreadIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into LockedThread factions!"));
            return true;
        }

        // This is Factions-Jerry.jar
        if (uuidTest.getDescription().getAuthors().contains("CmdrKittens") && uuidTest.getDescription().getAuthors().contains("Jerry")) {
            FactionIntegrations.integration = new JerryIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into FactionsUUID (Jerry's Fork)!"));
            return true;
        }

        // This is FactionsUUID.jar
        if (uuidTest.getDescription().getAuthors().contains("CmdrKittens")) {
            FactionIntegrations.integration = new UUIDIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into FactionsUUID!"));
            return true;
        }

        // This is Factions-Jartex.jar
        if (uuidTest.getDescription().getAuthors().contains("JustThiemo")) {
            FactionIntegrations.integration = new JartexIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into FactionsUUID (Jartex)!"));
            return true;
        }

        return false;
    }

    private static String c(String s) {
        return StringUtil.t(s);
    }
}
