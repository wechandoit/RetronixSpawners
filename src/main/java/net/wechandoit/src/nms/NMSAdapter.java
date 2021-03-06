package net.wechandoit.src.nms;

import net.wechandoit.src.spawners.SyncedCreatureSpawner;
import net.wechandoit.src.utils.enums.SpawnCause;
import org.bukkit.Achievement;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface NMSAdapter {

    /*
     *   Entity methods
     */

    <T extends Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer);

    Zombie spawnZombieVillager(Villager villager);

    void setInLove(Animals entity, Player breeder, boolean inLove);

    boolean isInLove(Animals entity);

    boolean isAnimalFood(Animals animal, ItemStack itemStack);

    int getEntityExp(LivingEntity livingEntity);

    boolean canDropExp(LivingEntity livingEntity);

    void updateLastDamageTime(LivingEntity livingEntity);

    void setHealthDirectly(LivingEntity livingEntity, double health);

    void setEntityDead(LivingEntity livingEntity, boolean dead);

    int getEggLayTime(Chicken chicken);

    void setNerfedEntity(LivingEntity livingEntity, boolean nerfed);

    void setKiller(LivingEntity livingEntity, Player killer);

    boolean canSpawnOn(Entity entity, Location location);

    List<Entity> getNearbyEntities(Location location, int range, Predicate<Entity> filter);

    default float getItemInMainHandDropChance(EntityEquipment entityEquipment){
        return entityEquipment.getItemInHandDropChance();
    }

    default float getItemInOffHandDropChance(EntityEquipment entityEquipment){
        return entityEquipment.getItemInHandDropChance();
    }

    default void setItemInMainHand(EntityEquipment entityEquipment, ItemStack itemStack){
        entityEquipment.setItemInHand(itemStack);
    }

    default void setItemInOffHand(EntityEquipment entityEquipment, ItemStack itemStack){
        entityEquipment.setItemInHand(itemStack);
    }

    default ItemStack getItemInOffHand(EntityEquipment entityEquipment){
        return entityEquipment.getItemInHand();
    }

    boolean shouldArmorBeDamaged(ItemStack itemStack);

    default String getEndermanCarried(Enderman enderman){
        MaterialData materialData = enderman.getCarriedMaterial();
        //noinspection deprecation
        return materialData.getItemType() + ":" + materialData.getData();
    }

    default byte getMooshroomType(MushroomCow mushroomCow){
        return 0;
    }

    default void setTurtleEgg(Entity turtle){

    }

    default Location getTurtleHome(Entity turtle){
        return null;
    }

    default void setTurtleEggsAmount(Block turtleEggBlock, int amount){

    }

    default void handleSweepingEdge(Player attacker, ItemStack usedItem, LivingEntity target, double damage){

    }

    default String getCustomName(Entity entity){
        return entity.getCustomName();
    }

    default void setCustomName(Entity entity, String name){
        entity.setCustomName(name);
    }

    default boolean isCustomNameVisible(Entity entity){
        return entity.isCustomNameVisible();
    }

    default void setCustomNameVisible(Entity entity, boolean visibleName){
        entity.setCustomNameVisible(visibleName);
    }

    /*
     *   Spawner methods
     */

    SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner);

    boolean isRotatable(Block block);

    /*
     *   Item methods
     */

    Enchantment getGlowEnchant();

    ItemStack getPlayerSkull(String texture);

    default boolean isDroppedItem(Entity entity){
        return entity instanceof Item;
    }

    /*
     *   World methods
     */

    default void grandAchievement(Player player, EntityType victim, String name){
        grandAchievement(player, "", name);
    }

    default void grandAchievement(Player player, String criteria, String name){
        Achievement achievement = Achievement.valueOf(name);
        if(!player.hasAchievement(achievement))
            player.awardAchievement(achievement);
    }

    void playPickupAnimation(LivingEntity livingEntity, Item item);

    void playDeathSound(LivingEntity entity);

    void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra);

    void playSpawnEffect(LivingEntity livingEntity);

    default Object getBlockData(Material type, short data){
        throw new UnsupportedOperationException("Not supported in this Minecraft version.");
    }

    default void attemptJoinRaid(Player player, Entity raider){

    }

    default boolean attemptToWaterLog(Block block){
        return false;
    }

    default void startEntityListen(World world){

    }

    /*
     *   Tag methods
     */

    void updateEntity(LivingEntity source, LivingEntity target);

    String serialize(ItemStack itemStack);

    ItemStack deserialize(String serialized);

    ItemStack setTag(ItemStack itemStack, String key, Object value);

    <T> T getTag(ItemStack itemStack, String key, Class<T> valueType, Object def);

    /*
     *   Other methods
     */

    default Object getChatMessage(String message){
        return message;
    }

}