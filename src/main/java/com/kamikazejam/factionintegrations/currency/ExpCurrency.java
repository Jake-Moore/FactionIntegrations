package com.kamikazejam.factionintegrations.currency;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.integrations.JartexIntegration;
import com.kamikazejam.factionintegrations.integrations.interfaces.KFaction;
import com.kamikazejam.factionintegrations.utils.EXP;
import org.bukkit.entity.Player;

public class ExpCurrency implements Currency {

    @Override
    public double getBalance(String id) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i instanceof JartexIntegration) {
            JartexIntegration integration = (JartexIntegration) i;
            return integration.getExp(id);
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
        if (i instanceof JartexIntegration) {
            JartexIntegration integration = (JartexIntegration) i;
            integration.addExp(id, (int) add);
            return;
        }

        //oof
        if (i.getOnlineMembers(id).size() == 0) { return; }

        EXP.giveExp(i.getOnlineMembers(id).get(0), (int) add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i instanceof JartexIntegration) {
            JartexIntegration integration = (JartexIntegration) i;
            integration.subtractMoney(id, remove);
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
