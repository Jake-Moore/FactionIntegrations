package com.kamikazejam.factionintegrations.currency;

import com.kamikazejam.factionintegrations.integrations.KFaction;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class Currency {
    private final @NotNull KFaction integration;
    public Currency(@NotNull KFaction integration) {
        this.integration = integration;
    }

    public abstract double getBalance(String id);

    public abstract void addBalance(String id, double add);

    public abstract void subtractBalance(String id, double remove);
}
