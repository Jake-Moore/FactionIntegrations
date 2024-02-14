package com.kamikazejam.factionintegrations.currency;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.integrations.KFaction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class MoneyCurrency implements Currency {

    @Override
    public double getBalance(String id) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i instanceof JartexIntegration) {
            JartexIntegration integration = (JartexIntegration) i;
            return integration.getMoney(id);
        }

        double total = 0;
        for (UUID player : i.getAllMembers(id)) {
            total += FactionIntegrations.getEconomy().getBalance(Bukkit.getOfflinePlayer(player));
        }
        return total;
    }

    @Override
    public void addBalance(String id, double add) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i instanceof JartexIntegration) {
            JartexIntegration integration = (JartexIntegration) i;
            integration.addMoney(id, add);
            return;
        }

        OfflinePlayer leader = Bukkit.getOfflinePlayer(i.getLeader(id));
        FactionIntegrations.getEconomy().depositPlayer(leader, add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i instanceof JartexIntegration) {
            JartexIntegration integration = (JartexIntegration) i;
            integration.subtractMoney(id, remove);
            return;
        }

        double toRemove = remove;
        for (UUID player : i.getAllMembers(id)) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

            if (FactionIntegrations.getEconomy().getBalance(offlinePlayer) >= toRemove) {
                FactionIntegrations.getEconomy().withdrawPlayer(offlinePlayer, toRemove);
                return;
            } else {
                toRemove -= FactionIntegrations.getEconomy().getBalance(offlinePlayer);
                FactionIntegrations.getEconomy().withdrawPlayer(offlinePlayer, FactionIntegrations.getEconomy().getBalance(offlinePlayer));
            }
        }
    }
}
