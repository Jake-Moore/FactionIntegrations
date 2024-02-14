package com.kamikazejam.factionintegrations.event;

import org.bukkit.event.Event;

public abstract class KFactionEvent extends Event {
    private final String faction;

    public KFactionEvent(String faction){
        this.faction = faction;
    }

    public String getFaction() {
        return faction;
    }
}
