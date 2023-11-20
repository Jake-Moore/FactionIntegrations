package com.kamikazejam.factionintegrations.event;

import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public abstract class KPlayerEvent extends KFactionEvent {
    private final String playerID;

    private final Reason reason;

    public KPlayerEvent(String faction, String playerID, Reason reason){
        super(faction);
        this.playerID = playerID;
        this.reason = reason;
    }

    public String getPlayer() {
        return playerID;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        LEAVE,
        KICK,
        JOIN,
        CREATE,
        DISBAND,
        RESET,
        COMMAND,
        LEADER,
        JOINOTHER
    }


    @Override
    public HandlerList getHandlers() { return handlerList; }
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private static final HandlerList handlerList = new HandlerList();

}
