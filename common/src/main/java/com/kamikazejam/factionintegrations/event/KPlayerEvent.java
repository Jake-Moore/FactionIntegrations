package com.kamikazejam.factionintegrations.event;

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
}
