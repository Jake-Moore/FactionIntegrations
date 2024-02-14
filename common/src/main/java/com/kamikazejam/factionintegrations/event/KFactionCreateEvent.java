package com.kamikazejam.factionintegrations.event;

import org.bukkit.event.HandlerList;

public class KFactionCreateEvent extends KFactionEvent{
    public KFactionCreateEvent(String faction) {
        super(faction);
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
