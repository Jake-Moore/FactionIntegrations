package com.kamikazejamplugins.factionintegrations.event;

import org.bukkit.event.HandlerList;

public class KPlayerLeaveEvent extends KPlayerEvent{
    public KPlayerLeaveEvent(String faction, String playerName, Reason reason) {
        super(faction, playerName, reason);
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
