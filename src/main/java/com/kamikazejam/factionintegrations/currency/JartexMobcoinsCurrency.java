package com.kamikazejam.factionintegrations.currency;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.integrations.JartexIntegration;
import com.kamikazejam.factionintegrations.integrations.interfaces.KFaction;
import net.crafti.common.member.Members;
import net.crafti.common.modules.modules.mobcoins.MobCoinsMember;

import java.util.UUID;

public class JartexMobcoinsCurrency implements Currency {

    @Override
    public double getBalance(String id) {
        KFaction i = FactionIntegrations.getIntegration();
        if (i instanceof JartexIntegration) {
            JartexIntegration integration = (JartexIntegration) i;
            return integration.getMobcoins(id);
        }

        double total = 0;
        for (UUID player : i.getAllMembers(id)) {
            MobCoinsMember member = Members.getMember(player, MobCoinsMember.class);
            if (member == null) { continue; }
            total += member.getBalance();
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

        UUID leader = i.getLeader(id);
        MobCoinsMember member = Members.getMember(leader, MobCoinsMember.class);
        if (member == null) { return; }
        member.addMobCoins((int) add);
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
        for (UUID player : i.getAllMembers(id)) {
            MobCoinsMember member = Members.getMember(player, MobCoinsMember.class);
            if (member == null) { continue; }
            if (member.getMobCoins() >= toRemove) {
                member.removeMobCoins(toRemove);
                return;
            }else {
                toRemove -= member.getMobCoins();
                member.removeMobCoins(member.getMobCoins());
            }
        }
    }
}
