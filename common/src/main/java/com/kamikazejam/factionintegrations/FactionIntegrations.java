package com.kamikazejam.factionintegrations;

import com.kamikazejam.factionintegrations.integrations.KFaction;
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

    private static boolean setupFactions() {
        final Plugin uuidTest = Bukkit.getPluginManager().getPlugin("Factions");
        if (uuidTest == null) { return false; }

        if (uuidTest.getDescription().getAuthors().contains("Elapsed")) {
            FactionIntegrations.integration = createIntegration("AtlasIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into AtlasFactions!"));
            return true;
        }

        // This is Factions-Joseph.jar
        if (uuidTest.getDescription().getAuthors().contains("Cayorion")) {
            FactionIntegrations.integration = new MCoreIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into Joseph MassiveCore Factions!"));
            return true;
        }

        // SupremeFactions is now GucciFactions (not adding support for it)
        // TODO: GucciFactions integration?

        if (uuidTest.getDescription().getAuthors().contains("Dominion")) {
            FactionIntegrations.integration = new DominionIntegration();
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into DominionFactions!"));
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

        // This is FactionsUUID.jar (written using Golfing's fork)
        if (uuidTest.getDescription().getAuthors().contains("CmdrKittens")) {
            FactionIntegrations.integration = createIntegration("UUIDIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into FactionsUUID!"));
            return true;
        }

        // This is Factions-Jartex.jar
        if (uuidTest.getDescription().getAuthors().contains("JustThiemo")) {
            FactionIntegrations.integration = createIntegration("JartexIntegration");
            Bukkit.getConsoleSender().sendMessage(c("&a&lFactionsKore &7- &aSuccessfully hooked into FactionsUUID (Jartex)!"));
            return true;
        }

        return false;
    }
    private static KFaction createIntegration(String name) {
        try {
            String currPackage = FactionIntegrations.class.getPackage().getName();
            String target = currPackage + ".integrations." + name;
            return (KFaction) Class.forName(target).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to create integration '" + name + "'", e);
        }
    }

    private static String c(String s) {
        return StringUtil.t(s);
    }
}
