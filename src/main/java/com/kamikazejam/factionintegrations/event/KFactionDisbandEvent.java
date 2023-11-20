package com.kamikazejam.factionintegrations.event;

import lombok.Getter;

public class KFactionDisbandEvent extends KFactionEvent{
    @Getter
    private final String factionTag;

    public KFactionDisbandEvent(String faction, String factionTag) {
        super(faction);
        this.factionTag = factionTag;
    }
}
