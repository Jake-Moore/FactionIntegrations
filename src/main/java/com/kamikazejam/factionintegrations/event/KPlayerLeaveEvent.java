package com.kamikazejam.factionintegrations.event;

public class KPlayerLeaveEvent extends KPlayerEvent{
    public KPlayerLeaveEvent(String faction, String playerName, Reason reason) {
        super(faction, playerName, reason);
    }
}
