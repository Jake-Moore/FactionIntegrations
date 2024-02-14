package com.kamikazejam.factionintegrations.currency;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CurrencyHandler {
    public static final MoneyCurrency MONEY = new MoneyCurrency();
    public static final ExpCurrency EXP = new ExpCurrency();

    public static MoneyCurrency getMoneyCurrency() {
        return MONEY;
    }

    public static ExpCurrency getExpCurrency() {
        return EXP;
    }

    public static Currency JARTEX_MOBCOINS = null;
    public static @Nullable Currency getTokensCurrency() {
        if (Bukkit.getPluginManager().getPlugin("Grizzly") != null && JARTEX_MOBCOINS == null) {
            String target = "com.kamikazejam.factionintegrations.currency.JartexMobcoinsCurrency";
            try {
                Class<?> clazz = Class.forName(target);
                JARTEX_MOBCOINS = (Currency) clazz.newInstance();
                return JARTEX_MOBCOINS;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Failed to load JartexMobcoinsCurrency", e);
            }
        }
        return null;
    }
}
