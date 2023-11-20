package com.kamikazejam.factionintegrations.utils;

import org.bukkit.entity.Player;

import java.util.Locale;

@SuppressWarnings("unused")
public class EXP {
    public EXP() {}

    public static void setExp(Player target, String strAmount) {
        setExp(target, strAmount, false);
    }

    public static void setExp(Player target, int amount) {
        setExp(target, String.valueOf(amount), false);
    }

    private static void giveExp(Player target, String strAmount) {
        setExp(target, strAmount, true);
    }

    public static void giveExp(Player target, int amount) {
        giveExp(target, String.valueOf(amount));
    }

    private static void takeExp(Player target, String strAmount) {
        setExp(target, strAmount, true);
    }

    public static void takeExp(Player target, int amount) {
        takeExp(target, "-" + amount);
    }

    private static void setExp(Player target, String strAmount, boolean give) {
        try {
            Long.parseLong(strAmount);
            Integer.parseInt(strAmount);
        } catch (NumberFormatException var6) {
            return;
        }

        strAmount = strAmount.toLowerCase(Locale.ENGLISH);
        long amount;
        if (strAmount.contains("l")) {
            strAmount = strAmount.replaceAll("l", "");
            int neededLevel = Integer.parseInt(strAmount);
            if (give) {
                neededLevel += target.getLevel();
            }

            amount = (long)getExpToLevel(neededLevel);
            setTotalExperience(target, 0);
        } else {
            amount = Long.parseLong(strAmount);
        }

        if (give) {
            amount += (long)getTotalExperience(target);
        }

        if (amount > 2147483647L) {
            amount = 2147483647L;
        }

        if (amount < 0L) {
            amount = 0L;
        }

        setTotalExperience(target, (int)amount);
    }

    private static void setTotalExperience(Player player, int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        } else {
            player.setExp(0.0F);
            player.setLevel(0);
            player.setTotalExperience(0);
            int amount = exp;

            while(amount > 0) {
                int expToLevel = getExpAtLevel(player);
                amount -= expToLevel;
                if (amount >= 0) {
                    player.giveExp(expToLevel);
                } else {
                    amount += expToLevel;
                    player.giveExp(amount);
                    amount = 0;
                }
            }

        }
    }

    private static int getExpAtLevel(Player player) {
        return getExpAtLevel(player.getLevel());
    }

    private static int getExpAtLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else {
            return level >= 16 && level <= 30 ? 5 * level - 38 : 9 * level - 158;
        }
    }

    private static int getExpToLevel(int level) {
        int currentLevel = 0;

        int exp;
        for(exp = 0; currentLevel < level; ++currentLevel) {
            exp += getExpAtLevel(currentLevel);
        }

        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }

        return exp;
    }

    public static int getTotalExperience(Player player) {
        int exp = Math.round((float)getExpAtLevel(player) * player.getExp());

        for(int currentLevel = player.getLevel(); currentLevel > 0; exp += getExpAtLevel(currentLevel)) {
            --currentLevel;
        }

        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }

        return exp;
    }

    public static int getExpUntilNextLevel(Player player) {
        int exp = Math.round((float)getExpAtLevel(player) * player.getExp());
        int nextLevel = player.getLevel();
        return getExpAtLevel(nextLevel) - exp;
    }
}
