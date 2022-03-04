package net.wechandoit.src.listeners;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.google.common.collect.Sets;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;
import net.wechandoit.src.Main;
import net.wechandoit.src.config.ConfigData;
import net.wechandoit.src.entity.Drops;
import net.wechandoit.src.events.PlayerKillMobEvent;
import net.wechandoit.src.spawners.StackableSpawner;
import net.wechandoit.src.utils.MiscUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import uk.antiperson.stackmob.api.StackedEntity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EntityListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) return;

        ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(event.getEntity());
        if (activeMob != null) {
            if (activeMob.getType() != null)
                return;
        }

        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) event.getEntity();
            if (living != null && ((living.getHealth() - event.getDamage()) <= 0)) {

                if (event.getDamager().getType() == EntityType.PLAYER) {
                    Player player = (Player) event.getDamager();
                    int cleave = 1;
                    if ((SuperiorSkyblockAPI.getIslandAt(player.getLocation()) == null || MiscUtils.isPlayerOnIsland(player)) || SuperiorSkyblockAPI.getIslandAt(player.getLocation()).isSpawn() || player.isOp()) {
                        living.setHealth(living.getMaxHealth());

                        if (player.getItemInHand() != null) {
                            Map<Enchantment, Integer> enchantMap = player.getItemInHand().getEnchantments();
                            for (Enchantment e : enchantMap.keySet()) {
                                if (e.equals(Enchantment.SWEEPING_EDGE)) {
                                    cleave = enchantMap.get(Enchantment.SWEEPING_EDGE);
                                }
                            }
                        }

                        try {
                            StackedEntity stackedEntity = Main.getStackableEntityManager().getStackedEntity(event.getEntity());
                            if (stackedEntity.getSize() - cleave > 0) {
                                stackedEntity.setSize(stackedEntity.getSize() - cleave);
                                event.setCancelled(true);
                            } else {
                                event.getEntity().remove();
                                cleave = stackedEntity.getSize();
                                stackedEntity.setSize(0);
                            }
                            if (cleave == 0) cleave = 1;
                            PlayerKillMobEvent killMobEvent = new PlayerKillMobEvent(event.getEntity(), player, false, cleave);
                            Main.getInstance().getServer().getPluginManager().callEvent(killMobEvent);
                        } catch (IndexOutOfBoundsException exception) {
                            Main.getStackableEntityManager().addNewStack(event.getEntity());
                        }

                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event){
        if (event.getTarget() instanceof LivingEntity){
            ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(event.getEntity());
            if (activeMob != null) {
                if (activeMob.getType() != null)
                    return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            if (event.getEntity().getType() == EntityType.PLAYER) return;

            ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(event.getEntity());
            if (activeMob != null) {
                if (activeMob.getType() != null)
                    return;
            }

            if (event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID)
                event.getEntity().remove();

            if (event.getEntity() != null) {
                LivingEntity living = event.getEntity();
                Location location = living.getLocation();
                boolean cook = false;
                int looting = 1;
                int amount = 1;
                if (living != null) {
                    event.getDrops().clear();
                    event.setDroppedExp(0);

                    if (event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LAVA ||
                            event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                            event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.FIRE)
                        cook = true;

                    if (event.getEntity().getKiller() != null && event.getEntity().getKiller().getType() == EntityType.PLAYER) {
                        Player player = living.getKiller();
                        if (player.getItemInHand() != null) {
                            Map<Enchantment, Integer> enchantMap = player.getItemInHand().getEnchantments();
                            for (Enchantment e : enchantMap.keySet()) {
                                if (e.equals(Enchantment.FIRE_ASPECT)) {
                                    cook = true;
                                }

                                if (e.equals(Enchantment.LOOT_BONUS_MOBS)) {
                                    looting = 1 + enchantMap.get(Enchantment.LOOT_BONUS_MOBS);
                                }
                            }
                        }
                    }

                    if (living.getLastDamageCause().getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                            && living.getLastDamageCause().getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                        StackedEntity mob = Main.getStackableEntityManager().getStackedEntity(event.getEntity());
                        try {
                            amount = mob.getSize();
                            if (amount > 500) amount = 500;
                            mob.setSize(0);
                        } catch (Exception ignored) {

                        }
                    }

                    if (ConfigData.mobDropMap.containsKey(living.getType())) {
                        List<Drops> dropsList = ConfigData.mobDropMap.get(living.getType());
                        MiscUtils.calculateDrops(dropsList, amount, looting, cook, location);
                    }

                    if (living.getKiller() != null) {
                        Player player = living.getKiller();
                        if (ConfigData.mobXPMap.containsKey(living.getType())) {
                            List<Integer> mobXP = ConfigData.mobXPMap.get(living.getType());
                            Random random = new Random();
                            try {
                                int amt = mobXP.get(0);
                                if (mobXP.get(0) != mobXP.get(1))
                                    amt = random.nextInt(mobXP.get(1) - mobXP.get(0)) + mobXP.get(0);
                                if (amt > 0) {
                                    player.giveExp(amt);
                                }
                            } catch (IndexOutOfBoundsException ignored) {

                            }
                        }
                    }
                }
            }
        } catch (Exception ignored)
        {

        }
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (event.getEntity().getType().equals(EntityType.CHICKEN)) {
            if (entity.getPassenger() != null) {
                entity.remove();
            }
        }

        CreatureSpawner cs = event.getSpawner();
        StackableSpawner spawner = Main.getStackableSpawnerManager().getSpawnerFromLocation(cs.getLocation());

        //entity.setHealth(1);
        entity.getEquipment().setHelmet(null);
        entity.getEquipment().setChestplate(null);
        entity.getEquipment().setLeggings(null);
        entity.getEquipment().setBoots(null);
        entity.getEquipment().setItemInMainHand(null);
        growUp(entity);
        entity.setCanPickupItems(false);
        entity.setGravity(true);
        overrideBehavior(entity);

        try {
            Main.getStackableEntityManager().addNewStack(event.getEntity(), spawner.getAmount());
        } catch (NullPointerException ignored) {

        }

    }

    public void overrideBehavior(LivingEntity e) {
        try {
            EntityCreature c = (EntityCreature) ((CraftEntity) e).getHandle();
            //This gets the EntityCreature, we need it to change the values

            try {
                Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
                bField.setAccessible(true);
                Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
                cField.setAccessible(true);
                bField.set(c.goalSelector, Sets.newLinkedHashSet());
                bField.set(c.targetSelector, Sets.newLinkedHashSet());
                cField.set(c.goalSelector, Sets.newLinkedHashSet());
                cField.set(c.targetSelector, Sets.newLinkedHashSet());
                //this code clears fields B, C. so right now the mob wont walk

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        } catch (ClassCastException f) {
            e.setAI(false);
        }
    }

    public void growUp(LivingEntity e) {
        if (e instanceof Zombie) {
            Zombie zom = (Zombie) e;
            if (zom.isBaby()) {
                zom.setBaby(false);
            }
        }

        if (e instanceof ZombieVillager) {
            ZombieVillager zom = (ZombieVillager) e;
            if (zom.isBaby()) {
                zom.setBaby(false);
            }
        }

        if (e instanceof Husk) {
            Husk zom = (Husk) e;
            if (zom.isBaby()) {
                zom.setBaby(false);
            }
        }

        if (e instanceof PigZombie) {
            PigZombie pzm = (PigZombie) e;
            if (pzm.isBaby()) {
                pzm.setBaby(false);
            }
        }
    }

    @EventHandler
    public void onEggSpawnEvent(ItemSpawnEvent event) {
        Item ITEM = event.getEntity();

        if (ITEM.getItemStack().getType() == Material.EGG) {
            for (Entity entity : ITEM.getNearbyEntities(0.5D, 1.0D, 0.5D)) {
                if (entity.getType() == EntityType.CHICKEN) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onDmg(EntityDamageEvent e) {
        Entity ent = e.getEntity();
        if (ent instanceof Blaze || ent instanceof Enderman) {
            if (e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMiddle(EntityTeleportEvent event) {
        if (event.getEntityType().equals(EntityType.ENDERMAN)) {
            event.setCancelled(true);
        }
    }

}
