package net.wechandoit.src.utils;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import io.lumine.xikage.mythicmobs.MythicMobs;
import net.citizensnpcs.api.CitizensAPI;
import net.wechandoit.src.entity.Drops;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

import static org.bukkit.Bukkit.getServer;

public class MiscUtils {

    public static int getAmountOfStacks(int amount) {
        int count = amount / 64;
        if (amount % 64 != 0) count++;
        return count;
    }

    public static List<Material> swordList() {
        List<Material> meleeList = new ArrayList<>();

        meleeList.add(Material.WOOD_SWORD);
        meleeList.add(Material.STONE_SWORD);
        meleeList.add(Material.GOLD_SWORD);
        meleeList.add(Material.IRON_SWORD);
        meleeList.add(Material.DIAMOND_SWORD);

        return meleeList;
    }

    public static void calculateDrops(List<Drops> drops, int multiplier, int looting, boolean cook, Location dropLocation) {
        for (Drops drop : drops) {
            ItemStack itemStack = drop.getDrop(cook, looting);
            if (itemStack != null)
                dropDrops(itemStack, multiplier * itemStack.getAmount(), dropLocation);
        }
    }

    public static void dropDrops(ItemStack drop, int amount, Location dropLocation) {
        dropAllDrops(drop, amount, dropLocation, false);
    }


    public static void dropEggs(ItemStack drop, int amount, Location dropLocation) {
        dropAllDrops(drop, amount, dropLocation, true);
    }


    private static void dropAllDrops(ItemStack drop, int amount, Location dropLocation, boolean addEnchantment) {
        double inStacks = amount / 64;
        double floor = Math.floor(inStacks);
        double leftOver = amount % 64;
        for (int i = 1; i <= floor; i++) {
            ItemStack newStack = drop.clone();
            newStack.setAmount(drop.getMaxStackSize());
            dropLocation.getWorld().dropItemNaturally(dropLocation, newStack);
        }
        if (leftOver > 0.0D) {
            ItemStack newStack = drop.clone();
            newStack.setAmount((int) leftOver);
            dropLocation.getWorld().dropItemNaturally(dropLocation, newStack);
        }
    }

    public static List<Material> interactableBlockList() {
        List<Material> interactableBlockList = new ArrayList<>();

        interactableBlockList.add(Material.CHEST);
        interactableBlockList.add(Material.TRAPPED_CHEST);
        interactableBlockList.add(Material.FURNACE);
        interactableBlockList.add(Material.BURNING_FURNACE);
        interactableBlockList.add(Material.ENCHANTMENT_TABLE);
        interactableBlockList.add(Material.WORKBENCH);
        interactableBlockList.add(Material.DROPPER);
        interactableBlockList.add(Material.DISPENSER);
        interactableBlockList.add(Material.BED_BLOCK);
        interactableBlockList.add(Material.DARK_OAK_DOOR);
        interactableBlockList.add(Material.ACACIA_DOOR);
        interactableBlockList.add(Material.BIRCH_DOOR);
        interactableBlockList.add(Material.JUNGLE_DOOR);
        interactableBlockList.add(Material.SPRUCE_DOOR);
        interactableBlockList.add(Material.WOOD_DOOR);
        interactableBlockList.add(Material.WOODEN_DOOR);
        interactableBlockList.add(Material.IRON_DOOR_BLOCK);
        interactableBlockList.add(Material.TRAP_DOOR);
        interactableBlockList.add(Material.IRON_TRAPDOOR);
        interactableBlockList.add(Material.NOTE_BLOCK);
        interactableBlockList.add(Material.LEVER);
        interactableBlockList.add(Material.JUKEBOX);

        return interactableBlockList;
    }


    public static boolean isPlayerOnIsland(Player player) {
        if (isPluginOnServerAndEnabled("SuperiorSkyblock2")) {
            SuperiorPlayer ssPlayer = SuperiorSkyblockAPI.getPlayer(player);
            if (SuperiorSkyblockAPI.getPlayer(player).getIsland() == null)
                return false;
            if (SuperiorSkyblockAPI.getIslandAt(player.getLocation()) == null)
                return false;
            if (SuperiorSkyblockAPI.getIslandAt(player.getLocation()).getIslandMembers(true).isEmpty()) {
                if (!SuperiorSkyblockAPI.getIslandAt(player.getLocation()).getIslandMembers().contains(ssPlayer))
                    return false;
            }
            return true;
        }
        return false;
    }

    private static double distance(Location loc1, Location loc2) {
        return loc1.getWorld() != loc2.getWorld() ? Double.POSITIVE_INFINITY : loc1.distanceSquared(loc2);
    }

    public static <T extends Entity> Optional<T> getClosestBukkit(Location origin, Stream<T> objects) {
        Map<T, Double> distances = new HashMap<>();
        return objects.min((o1, o2) -> {
            if (!distances.containsKey(o1))
                distances.put(o1, distance(o1.getLocation(), origin));
            if (!distances.containsKey(o2))
                distances.put(o2, distance(o2.getLocation(), origin));

            return distances.get(o1).compareTo(distances.get(o2));
        });
    }

    public static boolean isStackable(Entity entity) {
        return (isMythicMob(entity) || (entity instanceof LivingEntity && !entity.getType().name().equals("ARMOR_STAND") && !(entity instanceof Player) && !isNPC(entity)));
    }

    public static boolean isNPC(Entity entity) {
        return isPluginOnServerAndEnabled("Citizens") && CitizensAPI.getNPCRegistry().isNPC(entity);
    }

    public static boolean isMythicMob(Entity entity) {
        return isPluginOnServerAndEnabled("MythicMobs") && MythicMobs.inst().getMobManager().isActiveMob(entity.getUniqueId());
    }

    public static boolean isPluginOnServerAndEnabled(String pluginName) {
        return getServer().getPluginManager().getPlugin(pluginName) != null && getServer().getPluginManager().isPluginEnabled(pluginName);
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumType, String name) {
        try {
            return Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static Enum[] getEnumValues(Class<?> clazz) {
        return (Enum[]) clazz.getEnumConstants();
    }
}
