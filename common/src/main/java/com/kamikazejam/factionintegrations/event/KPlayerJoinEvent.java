package com.kamikazejam.factionintegrations.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class KPlayerJoinEvent extends KPlayerEvent implements Cancellable {
    public KPlayerJoinEvent(String faction, String player, Reason reason) {
        super(faction, player, reason);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled;
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
