package com.kamikazejam.factionintegrations.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public abstract class KFactionEvent extends Event {
    private final String faction;

    public KFactionEvent(String faction){
        this.faction = faction;
    }

    public String getFaction() {
        return faction;
    }

    @Override
    public HandlerList getHandlers() { return handlerList; }
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private static final HandlerList handlerList = new HandlerList();

}
