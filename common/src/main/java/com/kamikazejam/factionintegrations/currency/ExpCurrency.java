package com.kamikazejam.factionintegrations.currency;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.integrations.KFaction;
import com.kamikazejam.factionintegrations.utils.EXP;
import org.bukkit.entity.Player;

public class ExpCurrency implements Currency {

    @Override
    public double getBalance(String id) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i.supportsExpOperations()) {
            return i.getExp(id);
        }

        double total = 0;
        for (Player player : i.getOnlineMembers(id)) {
            total += EXP.getTotalExperience(player);
        }
        return total;
    }

    @Override
    public void addBalance(String id, double add) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i.supportsExpOperations()) {
            i.addExp(id, (int) add);
            return;
        }

        //oof
        if (i.getOnlineMembers(id).isEmpty()) {
            return;
        }

        EXP.giveExp(i.getOnlineMembers(id).get(0), (int) add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i.supportsExpOperations()) {
            i.subtractMoney(id, remove);
            return;
        }

        int toRemove = (int) remove;
        for (Player player : i.getOnlineMembers(id)) {

            if (EXP.getTotalExperience(player) >= toRemove) {
                EXP.takeExp(player, toRemove);
                return;
            } else {
                toRemove -= EXP.getTotalExperience(player);
                EXP.takeExp(player, toRemove);
            }
        }
    }
}
