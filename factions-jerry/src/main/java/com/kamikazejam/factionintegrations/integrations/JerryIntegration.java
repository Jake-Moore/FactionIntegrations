package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.shield.ShieldIntegration;
import com.massivecraft.factions.*;
import com.massivecraft.factions.objects.Strike;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class JerryIntegration extends UUIDIntegration implements ShieldIntegration {

    private Method getBaseRegionOptions;

    private Method isShielded;

    private Method getUpgradesManager;

    private Method getUpgrade;

    private Method getBoost;

    private Method getUpgradeLevel;

    private Method getInventory;

    private Field playersInProcessField;

    private Method getWhereAlt;

    private Method getStrikes;

    private Method isAlt;

    public JerryIntegration() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        getBaseRegionOptions = Faction.class.getMethod("getBaseRegionOptions");

        getWhereAlt = Faction.class.getMethod("getFPlayersWhereAlt", boolean.class);

        isAlt = FPlayer.class.getMethod("isAlt");

        isShielded = Faction.class.getMethod("isShielded");

        getStrikes = Faction.class.getMethod("getStrikes");

        getUpgradesManager = FactionsPlugin.class.getMethod("getUpgradesManager");

        getUpgrade = Class.forName("com.massivecraft.factions.managers.UpgradesManager").getMethod("getUpgrade", String.class);

        Class<?> aClass = Class.forName("com.massivecraft.factions.objects.Upgrade");

        getBoost = aClass.getMethod("getBoost", int.class);

        getUpgradeLevel = Faction.class.getMethod("getUpgradeLevel", aClass);

        getInventory = Faction.class.getMethod("getInventory");

        playersInProcessField = Class.forName("com.massivecraft.factions.listeners.FactionsChestListener").getDeclaredField("playersToProcess");

        playersInProcessField.setAccessible(true);
    }

    @Override
    public boolean isBaseRegion(Location location, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return false;

        try {
            Set<FLocation> fLocationSet = (Set<FLocation>) getBaseRegionOptions.invoke(faction);

            return fLocationSet.contains(new FLocation(location));
        } catch (IllegalAccessException | InvocationTargetException exc) {

        }
        return false;
    }

    @Override
    public int getStrikes(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return 0;

        try {
            List<Strike> strikes = (List) getStrikes.invoke(faction);

            return strikes.stream().mapToInt(Strike::getPoints).sum();
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
        return 0;
    }

    @Override
    public boolean isShieldActiveNow(String id) {
        try {
            Faction factionById = Factions.getInstance().getFactionById(id);

            if (factionById == null) return false;

            return (boolean) isShielded.invoke(factionById);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
        return false;
    }

    @Override
    public long getMaxTnt(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return 0;

        try {
            Object upgradeManager = getUpgradesManager.invoke(FactionsPlugin.getInstance());

            Object upgrade = getUpgrade.invoke(upgradeManager, "tnt");

            int factionLevel = (int) getUpgradeLevel.invoke(faction, upgrade);

            return (int) getBoost.invoke(upgrade, factionLevel);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
        return 0L;
    }

    @Override
    public boolean isInFactionChest(Player player, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return false;

        Inventory inventory = getChestInventory(player, id);

        return inventory.getViewers().contains(player);
    }

    @Override
    public int getSize(String id) {
        try {
            Set invoke = (Set) getWhereAlt.invoke(Factions.getInstance().getFactionById(id), false);
            if (invoke == null) return 0;
            return invoke.size();
        } catch (IllegalAccessException | InvocationTargetException exc) {
            return 0;
        }
    }

    @Override
    public Inventory getChestInventory(Player player, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (faction == null) return null;
        try {
            return (Inventory) getInventory.invoke(faction);
        } catch (IllegalAccessException | InvocationTargetException exc) {

        }
        return null;
    }

    @Override
    public boolean isShieldRegionAt(String id, Location location) {
        return this.isBaseRegion(location, id);
    }

    @Override
    public boolean isShieldActive(String id) {
        return this.isShieldActiveNow(id);
    }

    @Override
    public boolean isAlt(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        try {
            return (boolean) isAlt.invoke(fPlayer);
        } catch (IllegalAccessException | InvocationTargetException ignored) {

        }
        return false;
    }
}

