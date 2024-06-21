package com.kamikazejam.factionintegrations.currency;

import com.kamikazejam.factionintegrations.integrations.KFaction;
import net.crafti.common.member.Members;
import net.crafti.common.modules.modules.mobcoins.MobCoinsMember;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class JartexMobcoinsCurrency extends Currency {

    public JartexMobcoinsCurrency(@NotNull KFaction integration) {
        super(integration);
    }

    @Override
    public double getBalance(String id) {
        KFaction i = this.getIntegration();
        if (i.supportsMobcoinsOperations()) {
            return i.getMobcoins(id);
        }

        double total = 0;
        for (UUID player : i.getAllMembers(id)) {
            MobCoinsMember member = Members.getMember(player, MobCoinsMember.class);
            if (member == null) {
                continue;
            }
            total += member.getBalance();
        }
        return total;
    }

    @Override
    public void addBalance(String id, double add) {
        KFaction i = this.getIntegration();
        if (i.supportsMobcoinsOperations()) {
            i.addMoney(id, add);
            return;
        }

        UUID leader = i.getLeader(id);
        MobCoinsMember member = Members.getMember(leader, MobCoinsMember.class);
        if (member == null) {
            return;
        }
        member.addMobCoins((int) add);
    }

    @Override
    public void subtractBalance(String id, double remove) {
        KFaction i = this.getIntegration();
        if (i.supportsMobcoinsOperations()) {
            i.subtractMoney(id, remove);
            return;
        }

        int toRemove = (int) remove;
        for (UUID player : i.getAllMembers(id)) {
            MobCoinsMember member = Members.getMember(player, MobCoinsMember.class);
            if (member == null) {
                continue;
            }
            if (member.getMobCoins() >= toRemove) {
                member.removeMobCoins(toRemove);
                return;
            } else {
                toRemove -= member.getMobCoins();
                member.removeMobCoins(member.getMobCoins());
            }
        }
    }
}
