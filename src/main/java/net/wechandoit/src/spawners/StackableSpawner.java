package net.wechandoit.src.spawners;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtinjector.NBTInjector;
import net.wechandoit.src.Main;
import net.wechandoit.src.stackable.StackableObject;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class StackableSpawner extends StackableObject {
    private UUID uniqueId;
    private CreatureSpawner cs;

    public StackableSpawner(int amount, Location location) {
        super(amount, location);
        uniqueId = UUID.randomUUID();
        cs = SyncedCreatureSpawner.of(getCreatureSpawner());
        Main.getInstance().getNMSSpawners().updateStackedSpawner(this);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public void setAmount(int amount) {
        super.setAmount(amount);
        NBTCompound comp = NBTInjector.getNbtData(getBlock().getState());
        comp.setInteger("spawner-amount", amount);
    }

    @Override
    public int getAmount() {
        int storedAmount = super.getAmount();
        NBTCompound comp = NBTInjector.getNbtData(getBlock().getState());
        try {
            int blockAmount = comp.getInteger("spawner-amount");
            if (blockAmount == 0) {
                setAmount(storedAmount);
                return storedAmount;
            } else if (storedAmount == blockAmount)
                return storedAmount;
            else
                return blockAmount;
        } catch (Exception e)
        {
            e.printStackTrace();
            setAmount(storedAmount);
            return storedAmount;
        }

    }

    public Block getBlock() {
        return getLocation().getBlock();
    }

    public CreatureSpawner getCreatureSpawner() {
        return ((CreatureSpawner) getBlock().getState());
    }

    public EntityType getEntityType() {
        return ((CreatureSpawner) getBlock().getState()).getSpawnedType();
    }
}
