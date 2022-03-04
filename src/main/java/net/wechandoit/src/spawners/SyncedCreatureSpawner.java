package net.wechandoit.src.spawners;

import net.wechandoit.src.Main;
import org.bukkit.block.CreatureSpawner;

public interface SyncedCreatureSpawner extends CreatureSpawner {

    Main plugin = Main.getInstance();

    static SyncedCreatureSpawner of(CreatureSpawner creatureSpawner){
        return creatureSpawner instanceof SyncedCreatureSpawner ? (SyncedCreatureSpawner) creatureSpawner :
                plugin.getNmsAdapter().createSyncedSpawner(creatureSpawner);
    }

    SpawnerCachedData readData();

}
