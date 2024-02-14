package com.kamikazejam.factionintegrations.event;

import org.bukkit.entity.Player;

public abstract class KLandEvent extends KFactionEvent {
    private final Integer[] location;

    private final String world;

    private final Player claimer;

    public KLandEvent(String faction, String world, Integer[] location, Player claimer) {
        super(faction);

        this.world = world;

        this.claimer = claimer;

        this.location = location;
    }

    public boolean hasClaimer() {
        return this.claimer != null;
    }

    public Player getClaimer() {
        return claimer;
    }

    public Integer[] getLocation() {
        return location;
    }

    public String getWorld() {
        return world;
    }
}
