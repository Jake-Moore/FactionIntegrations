package com.kamikazejam.factionintegrations.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class JerryGenEvent extends Event implements Cancellable {

    private final Player player;
    private final Location usedAt;
    private final boolean infiniteBlock;

    public JerryGenEvent(Player player, Location usedAt, boolean infiniteBlock){
        this.player = player;
        this.usedAt = usedAt;
        this.infiniteBlock = infiniteBlock;
    }

    public boolean isInfiniteBlock() {
        return infiniteBlock;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getUsedAt() {
        return usedAt;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private boolean isCancelled = false;

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
