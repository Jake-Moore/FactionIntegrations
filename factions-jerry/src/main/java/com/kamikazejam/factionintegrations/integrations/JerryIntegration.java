package com.kamikazejam.factionintegrations.integrations;

import com.kamikazejam.factionintegrations.event.KPowerLossEvent;
import com.kamikazejam.factionintegrations.shield.ShieldIntegration;
import com.massivecraft.factions.*;
import com.massivecraft.factions.event.PowerLossEvent;
import com.massivecraft.factions.managers.UpgradesManager;
import com.massivecraft.factions.objects.Strike;
import com.massivecraft.factions.objects.Upgrade;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;

import java.util.Set;

public class JerryIntegration extends UUIDIntegration implements ShieldIntegration {

    public JerryIntegration() {}

    @Override
    public boolean isBaseRegion(Location location, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);
        if (faction == null) return false;

        return faction.getBaseRegionOptions().contains(new FLocation(location));
    }

    @Override
    public int getStrikes(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);
        if (faction == null) return 0;

        return faction.getStrikes().stream().mapToInt(Strike::getPoints).sum();
    }

    @Override
    public boolean isShieldActiveNow(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);
        if (faction == null) return false;

        return faction.isShielded();
    }

    @Override
    public long getMaxTnt(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);
        if (faction == null) return 0;

        UpgradesManager manager = FactionsPlugin.getInstance().getUpgradesManager();
        Upgrade upgrade = manager.getUpgrade("tnt");
        int factionLevel = faction.getUpgradeLevel(upgrade);
        return upgrade.getBoost(factionLevel);
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
        Faction faction = Factions.getInstance().getFactionById(id);
        Set<FPlayer> set = faction.getFPlayersWhereAlt(false);
        return (set == null) ? 0 : set.size();
    }

    @Override
    public Inventory getChestInventory(Player player, String id) {
        Faction faction = Factions.getInstance().getFactionById(id);
        if (faction == null) return null;
        return faction.getInventory();
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
        return fPlayer.isAlt();
    }

    @EventHandler
    public void onPowerLoss(PowerLossEvent e) {
        KPowerLossEvent event = new KPowerLossEvent(e.getfPlayer().getPlayer());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) e.setCancelled(true);
    }
}

