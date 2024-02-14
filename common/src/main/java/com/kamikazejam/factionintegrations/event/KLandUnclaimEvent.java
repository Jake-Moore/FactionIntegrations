package com.kamikazejam.factionintegrations.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class KLandUnclaimEvent extends KLandEvent {
    public KLandUnclaimEvent(String faction, String string, Integer[] location, Player player) {
        super(faction, string, location, player);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private static final HandlerList handlerList = new HandlerList();
}
