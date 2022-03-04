package net.wechandoit.src.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class PlayerKillMobEvent
        extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean isCancelled;

    private Player player;
    private Entity entity;
    private int entitiesKilled;

    public PlayerKillMobEvent(Entity entity, Player player, boolean isCancelled, int entitiesKilled) {
        this.isCancelled = isCancelled;
        this.player = player;
        this.entity = entity;
        this.entitiesKilled = entitiesKilled;
    }

    public static HandlerList getHandlerList() { return handlers; }

    public int getEntitiesKilled() {
        return entitiesKilled;
    }

    public void setEntitiesKilled(int entitiesKilled) {
        this.entitiesKilled = entitiesKilled;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public boolean isCancelled() { return this.isCancelled; }




    public void setCancelled(boolean isCancelled) { this.isCancelled = isCancelled; }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
