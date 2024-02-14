package com.kamikazejam.factionintegrations.shield;

import org.bukkit.Location;

public interface ShieldIntegration {

    boolean isShieldRegionAt(String id, Location location);

    boolean isShieldActive(String id);
}
