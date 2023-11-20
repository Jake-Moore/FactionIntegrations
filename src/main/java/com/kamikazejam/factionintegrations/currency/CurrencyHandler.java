package com.kamikazejam.factionintegrations.currency;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class CurrencyHandler {
    public static final MoneyCurrency MONEY = new MoneyCurrency();
    public static final ExpCurrency EXP = new ExpCurrency();
    public static final JartexMobcoinsCurrency JARTEX_MOBCOINS = new JartexMobcoinsCurrency();

    public static MoneyCurrency getMoneyCurrency() {
        return MONEY;
    }

    public static ExpCurrency getExpCurrency() {
        return EXP;
    }

    public static @Nullable Currency getTokensCurrency() {
        if (Bukkit.getPluginManager().getPlugin("Grizzly") != null) {
            return JARTEX_MOBCOINS;
        }
        return null;
    }
}
