package net.wechandoit.src.nms;

import net.wechandoit.src.spawners.StackableSpawner;

public interface NMSSpawners {

    void updateStackedSpawner(StackableSpawner stackedSpawner);

    void registerSpawnConditions();

}
