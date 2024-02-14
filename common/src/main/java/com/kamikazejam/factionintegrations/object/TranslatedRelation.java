package com.kamikazejam.factionintegrations.object;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import org.bukkit.ChatColor;

@SuppressWarnings({"unused"})
public enum TranslatedRelation {

    MEMBER(5, ChatColor.GREEN),
    ALLY(4, ChatColor.LIGHT_PURPLE),
    TRUCE(3, ChatColor.AQUA),
    NEUTRAL(2, ChatColor.WHITE),
    ENEMY(1, ChatColor.RED);

    private final int value;
    private final ChatColor relationColor;
    TranslatedRelation(int value, ChatColor chatColor){
        this.value = value;
        this.relationColor = chatColor;
    }

    public int getValue(){
        return this.value;
    }

    public ChatColor getRelationColor(){
        if(!FactionIntegrations.getIntegration().isRelationColorsEnabled())return null;
        return this.relationColor;
    }

    public boolean isGreaterThan(TranslatedRelation translatedRelation){
        return this.value > translatedRelation.getValue();
    }

    public boolean isEqualTo(TranslatedRelation translatedRelation){
        return this.value == translatedRelation.getValue();
    }

    public boolean isLessThan(TranslatedRelation translatedRelation){
        return this.value < translatedRelation.getValue();
    }
}
