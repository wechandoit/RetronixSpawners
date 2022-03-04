package net.wechandoit.src.nms;

import net.minecraft.server.v1_12_R1.*;
import net.wechandoit.src.Main;
import net.wechandoit.src.spawners.StackableSpawner;
import net.wechandoit.src.spawning.SpawnCondition;
import net.wechandoit.src.utils.Fields;
import net.wechandoit.src.utils.MessageUtils;
import net.wechandoit.src.utils.MiscUtils;
import net.wechandoit.src.utils.Random;
import net.wechandoit.src.utils.enums.SpawnCause;
import net.wechandoit.src.utils.structures.EntityStorage;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import uk.antiperson.stackmob.api.StackedEntity;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_12_R1 implements NMSSpawners {

    private static final Main plugin = Main.getInstance();

    @Override
    public void updateStackedSpawner(StackableSpawner stackedSpawner) {
        World world = ((CraftWorld) stackedSpawner.getWorld()).getHandle();
        Location location = stackedSpawner.getLocation();

        TileEntity tileEntity = world.getTileEntity(new BlockPosition(location.getX(), location.getY(), location.getZ()));
        if (tileEntity instanceof TileEntityMobSpawner) {
            new StackedMobSpawner((TileEntityMobSpawner) tileEntity, stackedSpawner);
        }
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.j(position) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MUSHROOM_COW, EntityType.MULE, EntityType.PARROT, EntityType.PIG, EntityType.RABBIT,
                EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.WOLF, EntityType.ZOMBIE_HORSE
        );

        createCondition("ANIMAL_LIGHT_AND_COLD", (world, position) -> {
            BiomeBase biomeBase = world.getBiome(position);
            boolean coldBiome = biomeBase == Biomes.l || biomeBase == Biomes.Z;
            Block block = world.getType(position.down()).getBlock();
            return world.j(position) > 8 && block == (coldBiome ? Blocks.GRASS : Blocks.ICE);
        }, EntityType.POLAR_BEAR);

        createCondition("IN_SLIME_CHUNK_OR_SWAMP",
                (world, position) -> world.getBiome(position) == Biomes.h || world.getChunkAtWorldCoords(position)
                        .a(world.spigotConfig.slimeSeed).nextInt(10) == 0 && position.getY() < 40,
                EntityType.SLIME
        );

        createCondition("MONSTER_LIGHT", (world, position) -> {
                    if (world.getBrightness(EnumSkyBlock.SKY, position) > world.random.nextInt(32)) {
                        return false;
                    } else {
                        int lightLevel = world.getLightLevel(position);

                        if (world.X()) {
                            int j = world.ah();
                            world.c(10);
                            lightLevel = world.getLightLevel(position);
                            world.c(j);
                        }

                        return lightLevel <= world.random.nextInt(8);
                    }
                }, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.GIANT,
                EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.VEX, EntityType.VINDICATOR
        );

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.getDifficulty() != EnumDifficulty.PEACEFUL,
                EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT, EntityType.HUSK,
                EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER,
                EntityType.ILLUSIONER, EntityType.VEX, EntityType.VINDICATOR, EntityType.ELDER_GUARDIAN
        );

        createCondition("ON_GRASS",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.GRASS,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE
        );

        createCondition("ON_GRASS_OR_SAND_OR_SNOW", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block == Blocks.GRASS || block == Blocks.SAND || block == Blocks.SNOW;
        }, EntityType.RABBIT);

        createCondition("ON_MYCELIUM",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.MYCELIUM,
                EntityType.MUSHROOM_COW
        );

        createCondition("ON_TREE_OR_AIR", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block instanceof BlockLeaves || block == Blocks.GRASS ||
                    block instanceof BlockLogAbstract || block == Blocks.AIR;
        }, EntityType.PARROT);
    }

    private static void createCondition(String id, BiPredicate<World, BlockPosition> predicate, EntityType... entityTypes) {
        SpawnCondition spawnCondition = SpawnCondition.register(new SpawnCondition(id, MessageUtils.format(id)) {
            @Override
            public boolean test(Location location) {
                return predicate.test(((CraftWorld) location.getWorld()).getHandle(),
                        new BlockPosition(location.getX(), location.getY(), location.getZ()));
            }
        });
        plugin.addSpawnCondition(spawnCondition, entityTypes);
    }

    static class StackedMobSpawner extends MobSpawnerAbstract {

        private final World world;
        private final BlockPosition position;
        private final WeakReference<StackableSpawner> stackedSpawner;
        private final List<MobSpawnerData> mobs = new ArrayList<>();

        private MobSpawnerData spawnData = new MobSpawnerData();
        public int minSpawnDelay = 20;
        public int maxSpawnDelay = 80;
        public int spawnCount = 4;
        public int maxNearbyEntities = 6;
        public int requiredPlayerRange = 16;
        public int spawnRange = 4;
        public String failureReason = "";

        private int spawnedEntities = 0;

        StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackableSpawner stackedSpawner) {
            this.world = tileEntityMobSpawner.getWorld();
            this.position = tileEntityMobSpawner.getPosition();
            this.stackedSpawner = new WeakReference<>(stackedSpawner);

            if (!(tileEntityMobSpawner.getSpawner() instanceof StackedMobSpawner)) {
                MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();
                Fields.TILE_ENTITY_SPAWNER_ABSTRACT_SPAWNER.set(tileEntityMobSpawner, this);
                a(originalSpawner.b(new NBTTagCompound()));
                this.mobs.clear();
            }
        }

        @Override
        public MinecraftKey getMobName() {
            String s = this.spawnData.b().getString("id");
            return UtilColor.b(s) ? null : new MinecraftKey(s);
        }

        @Override
        public void setMobName(@Nullable MinecraftKey minecraftkey) {
            if (minecraftkey != null) {
                this.spawnData.b().setString("id", minecraftkey.toString());
            }
        }

        @Override
        public void a(NBTTagCompound nbttagcompound) {
            this.spawnDelay = nbttagcompound.getShort("Delay");
            this.mobs.clear();
            if (nbttagcompound.hasKeyOfType("SpawnPotentials", 9)) {
                NBTTagList nbttaglist = nbttagcompound.getList("SpawnPotentials", 10);

                for (int i = 0; i < nbttaglist.size(); ++i) {
                    this.mobs.add(new MobSpawnerData(nbttaglist.get(i)));
                }
            }

            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("SpawnData");
            if (!nbttagcompound1.hasKeyOfType("id", 8)) {
                nbttagcompound1.setString("id", "Pig");
            }

            this.a(new MobSpawnerData(1, nbttagcompound1));
            if (nbttagcompound.hasKeyOfType("MinSpawnDelay", 99)) {
                this.minSpawnDelay = nbttagcompound.getShort("MinSpawnDelay");
                this.maxSpawnDelay = nbttagcompound.getShort("MaxSpawnDelay");
                this.spawnCount = nbttagcompound.getShort("SpawnCount");
            }

            if (nbttagcompound.hasKeyOfType("MaxNearbyEntities", 99)) {
                this.maxNearbyEntities = nbttagcompound.getShort("MaxNearbyEntities");
                this.requiredPlayerRange = nbttagcompound.getShort("RequiredPlayerRange");
            }

            if (nbttagcompound.hasKeyOfType("SpawnRange", 99)) {
                this.spawnRange = nbttagcompound.getShort("SpawnRange");
            }
        }

        @Override
        public NBTTagCompound b(NBTTagCompound nbttagcompound) {
            MinecraftKey minecraftkey = this.getMobName();

            if (minecraftkey != null) {
                nbttagcompound.setShort("Delay", (short) this.spawnDelay);
                nbttagcompound.setShort("MinSpawnDelay", (short) this.minSpawnDelay);
                nbttagcompound.setShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
                nbttagcompound.setShort("SpawnCount", (short) this.spawnCount);
                nbttagcompound.setShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
                nbttagcompound.setShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
                nbttagcompound.setShort("SpawnRange", (short) this.spawnRange);
                nbttagcompound.set("SpawnData", this.spawnData.b().clone());
                NBTTagList nbttaglist = new NBTTagList();
                if (!this.mobs.isEmpty()) {
                    for (MobSpawnerData mobSpawnerData : this.mobs)
                        nbttaglist.add(mobSpawnerData.a());
                } else {
                    nbttaglist.add(this.spawnData.a());
                }

                nbttagcompound.set("SpawnPotentials", nbttaglist);
            }

            return nbttagcompound;
        }

        @Override
        public void a(MobSpawnerData mobspawnerdata) {
            this.spawnData = mobspawnerdata;
            IBlockData blockData = world.getType(position);
            world.notify(position, blockData, blockData, 4);
        }

        @Override
        public void a(int i) {
            world.playBlockAction(position, Blocks.MOB_SPAWNER, i, 0);
        }

        @Override
        public World a() {
            return world;
        }

        @Override
        public BlockPosition b() {
            return position;
        }

        @Override
        public void c() {
            StackableSpawner stackedSpawner = this.stackedSpawner.get();

            if (stackedSpawner == null) {
                super.c();
                return;
            }

            if (!hasNearbyPlayers()) {
                failureReason = "There are no nearby players.";
                return;
            }

            if (this.spawnDelay == -1)
                resetSpawnDelay();

            if (this.spawnDelay > 0) {
                --this.spawnDelay;
                return;
            }

            org.bukkit.entity.Entity demoEntityBukkit = generateEntity(position.getX(), position.getY(), position.getZ(), false);

            if (demoEntityBukkit == null) {
                resetSpawnDelay();
                return;
            }

            if (!MiscUtils.isStackable(demoEntityBukkit)) {
                super.c();
                return;
            }

            StackedEntity demoEntity = Main.getStackableEntityManager().getStackedEntity(demoEntityBukkit);

            Entity demoNMSEntity = ((CraftEntity) demoEntityBukkit).getHandle();

            int stackAmount = stackedSpawner.getAmount();

            List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), new AxisAlignedBB(
                    position.getX(), position.getY(), position.getZ(),
                    position.getX() + 1, position.getY() + 1, position.getZ() + 1
            ).g(this.spawnRange));

            StackedEntity targetEntity = getTargetEntity(stackedSpawner, demoEntity, nearbyEntities);

            if (targetEntity == null && nearbyEntities.size() >= this.maxNearbyEntities) {
                failureReason = "There are too many nearby entities.";
                return;
            }

            int spawnCount = Random.nextInt(1, this.spawnCount, stackAmount);

            int amountPerEntity = 1;
            int mobsToSpawn;

            short particlesAmount = 0;

            mobsToSpawn = spawnCount;


            if (mobsToSpawn > 0) {
                amountPerEntity = Math.min(mobsToSpawn, 9999);
                mobsToSpawn = mobsToSpawn / amountPerEntity;
            }

            for (int i = 0; i < mobsToSpawn; i++) {
                double x = position.getX() + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;
                double y = position.getY() + world.random.nextInt(3) - 1;
                double z = position.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;

                org.bukkit.entity.Entity bukkitEntity = generateEntity(x, y, z, true);

                if (bukkitEntity == null) {
                    resetSpawnDelay();
                    return;
                }

                Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();

                boolean hasSpace = !(nmsEntity instanceof EntityInsentient) || ((EntityInsentient) nmsEntity).canSpawn();

                if (!hasSpace) {
                    failureReason = "Not enough space to spawn the entity.";
                    continue;
                }

                Location location = new Location(world.getWorld(), x, y, z);

                /*SpawnCondition failedCondition = plugin.getSpawnConditions(demoEntityBukkit.getType())
                        .stream().filter(spawnCondition -> !spawnCondition.test(location)).findFirst().orElse(null);

                if (failedCondition != null) {
                    failureReason = "Cannot spawn entities due to " + failedCondition.getName() + " restriction.";
                    System.out.println(failureReason);
                    continue;
                }*/

                if (handleEntitySpawn(bukkitEntity, stackedSpawner, amountPerEntity, particlesAmount <= this.spawnCount)) {
                    spawnedEntities += amountPerEntity;
                    particlesAmount++;
                }
            }

            if (spawnedEntities >= stackAmount)
                resetSpawnDelay();
        }

        private boolean hasNearbyPlayers() {
            return world.isPlayerNearby(position.getX() + 0.5D, position.getY() + 0.5D,
                    position.getZ() + 0.5D, requiredPlayerRange);
        }

        private void resetSpawnDelay() {
            if (maxSpawnDelay <= minSpawnDelay) {
                spawnDelay = minSpawnDelay;
            } else {
                spawnDelay = minSpawnDelay + world.random.nextInt(maxSpawnDelay - minSpawnDelay);
            }

            if (!this.mobs.isEmpty()) {
                a(WeightedRandom.a(this.a().random, this.mobs));
            }

            spawnedEntities = 0;

            a(1);
        }

        private org.bukkit.entity.Entity generateEntity(double x, double y, double z, boolean rotation) {
            NBTTagCompound entityCompound = this.spawnData.b();
            Entity entity = ChunkRegionLoader.a(entityCompound, world, x, y, z, false);
            return entity == null ? null : entity.getBukkitEntity();
        }

        private boolean handleEntitySpawn(org.bukkit.entity.Entity bukkitEntity, StackableSpawner stackedSpawner, int amountPerEntity, boolean spawnParticles) {
            Entity entity = ((CraftEntity) bukkitEntity).getHandle();
            StackedEntity stackedEntity = null;

            EntityStorage.setMetadata(bukkitEntity, "spawn-cause", SpawnCause.SPAWNER);

            if (amountPerEntity > 1) {
                stackedEntity = Main.getStackableEntityManager().getStackedEntity(bukkitEntity);
                stackedEntity.setSize(amountPerEntity);
            }

            if (entity instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient) entity;

                if (this.spawnData.b().d() == 1 && this.spawnData.b().hasKeyOfType("id", 8)) {
                    ((EntityInsentient) entity).prepare(world.D(new BlockPosition(entity)), null);
                }

                entityinsentient.fromMobSpawner = true;
            }

            if (CraftEventFactory.callSpawnerSpawnEvent(entity, position).isCancelled()) {
                stackedEntity.setSize(0);
            } else {
                ChunkRegionLoader.a(entity, world, CreatureSpawnEvent.SpawnReason.SPAWNER);

                if (spawnParticles)
                    world.triggerEffect(2004, position, 0);

                if (entity instanceof EntityInsentient) {
                    ((EntityInsentient) entity).doSpawnEffect();
                }

                return true;
            }

            return false;
        }

        private StackedEntity getTargetEntity(StackableSpawner stackedSpawner, StackedEntity demoEntity,
                                              List<? extends Entity> nearbyEntities) {

            Optional<CraftEntity> closestEntity = MiscUtils.getClosestBukkit(stackedSpawner.getLocation(),
                    nearbyEntities.stream().map(Entity::getBukkitEntity).filter(entity ->
                            MiscUtils.isStackable(entity) && !Main.getStackableEntityManager().getStackedEntity(entity).isStackingPrevented()));

            return closestEntity.map(Main.getStackableEntityManager()::getStackedEntity).orElse(null);
        }

    }

}
