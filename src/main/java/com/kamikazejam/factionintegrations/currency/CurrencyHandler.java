package com.kamikazejam.factionintegrations.currency;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CurrencyHandler {

    private static @Nullable MoneyCurrency MONEY = null;
    public static MoneyCurrency getMoneyCurrency() {
        if (MONEY == null) {
            MONEY = new MoneyCurrency(FactionIntegrations.getIntegration());
        }
        return MONEY;
    }

    private static @Nullable ExpCurrency EXP = null;
    public static ExpCurrency getExpCurrency() {
        if (EXP == null) {
            EXP = new ExpCurrency(FactionIntegrations.getIntegration());
        }
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
