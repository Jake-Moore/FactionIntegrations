package com.kamikazejam.factionintegrations.event;

import lombok.Getter;
import org.bukkit.event.HandlerList;

public class KFactionDisbandEvent extends KFactionEvent {
    @Getter
    private final String factionTag;

    public KFactionDisbandEvent(String faction, String factionTag) {
        super(faction);
        this.factionTag = factionTag;
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
