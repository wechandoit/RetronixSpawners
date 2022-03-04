package net.wechandoit.src.stackable;

import org.bukkit.Location;
import org.bukkit.World;

public abstract class StackableObject {
    private int amount;
    private Location location;

    public StackableObject(int amount, Location location) {
        this.amount = amount;
        this.location = location;
    }

    public World getWorld() {
        return location.getWorld();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
