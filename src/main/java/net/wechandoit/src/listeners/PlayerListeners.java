package net.wechandoit.src.listeners;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import net.wechandoit.src.Main;
import net.wechandoit.src.config.ConfigData;
import net.wechandoit.src.entity.Drops;
import net.wechandoit.src.events.PlayerKillMobEvent;
import net.wechandoit.src.events.SpawnerBreakEvent;
import net.wechandoit.src.events.SpawnerPlaceEvent;
import net.wechandoit.src.gui.GUIManager;
import net.wechandoit.src.nbt.NBT;
import net.wechandoit.src.spawners.StackableSpawner;
import net.wechandoit.src.utils.ItemBuilder;
import net.wechandoit.src.utils.MessageUtils;
import net.wechandoit.src.utils.MiscUtils;
import net.wechandoit.src.utils.UMaterial;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onPlayerClickInInventory(InventoryClickEvent event) {
        Player player = ((Player) event.getWhoClicked());
        if (event.getClickedInventory() != null) {
            if (event.getClickedInventory().getName().equals(MessageUtils.chat("&8Spawner Debug Menu"))) {
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                    Location location = getLocationFromSpawnerDebugItem(event.getCurrentItem());
                    player.teleport(location);
                } else if (getPage(event.getCurrentItem()) > 0) {
                    player.openInventory(GUIManager.getDebugSpawnerMenu().get(getPage(event.getCurrentItem()) - 1));
                }
            } else if (event.getClickedInventory().getName().equals(MessageUtils.chat("&8Spawner Shop"))) {
                event.setCancelled(true);
                if (getSpawnerType(event.getCurrentItem()) != null) {
                    EntityType type = getSpawnerType(event.getCurrentItem());
                    int price = 0;
                    if (ConfigData.mobToPriceMap.containsKey(type))
                        price = ConfigData.mobToPriceMap.get(type);
                    if (Main.getEconomy().getBalance(player) >= price) {
                        Main.getEconomy().withdrawPlayer(player, price);
                        player.getInventory().addItem(ItemBuilder.getSpawnerItem(type, 1));
                        player.sendMessage(MessageUtils.chat("&cYou bought 1 " + MessageUtils.getFormattedType(type.name()) + " spawner for $" + price));
                    } else {
                        player.closeInventory();
                        player.sendMessage(MessageUtils.chat("&cYou do not have enough money to buy this spawner!"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerKillMob(PlayerKillMobEvent event) {
        if (event.getEntity() != null) {

            Entity entity = event.getEntity();
            Player player = event.getPlayer();
            boolean cook = false;
            int looting = 1;
            if (player.getItemInHand() != null) {
                Map<Enchantment, Integer> enchantMap = player.getItemInHand().getEnchantments();
                if (!enchantMap.isEmpty())
                for (Enchantment e : enchantMap.keySet()) {
                    if (e.equals(Enchantment.FIRE_ASPECT)) {
                        cook = true;
                    }

                    if (e.equals(Enchantment.LOOT_BONUS_MOBS)) {
                        looting = 1 + enchantMap.get(Enchantment.LOOT_BONUS_MOBS);
                    }
                }
            }

            if (ConfigData.mobDropMap.containsKey(entity.getType())) {
                List<Drops> dropsList = ConfigData.mobDropMap.get(entity.getType());
                MiscUtils.calculateDrops(dropsList, event.getEntitiesKilled(), looting, cook, entity.getLocation());
            }

            for (int i = 0; i < event.getEntitiesKilled(); i++) {
                if (ConfigData.mobXPMap.containsKey(entity.getType())) {
                    List<Integer> mobXP = ConfigData.mobXPMap.get(entity.getType());
                    Random random = new Random();
                    int amount = mobXP.get(0);
                    if (mobXP.get(0) != mobXP.get(1))
                        amount = random.nextInt(mobXP.get(1) - mobXP.get(0)) + mobXP.get(0);
                    if (amount > 0) {
                        player.giveExp(amount);
                    }
                }
            }

        }
    }

    @EventHandler
    public static void getSpawnerAmount(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block == null || block.getType() == Material.AIR) {
            return;
        }
        if ((player.getItemInHand() == null || player.getItemInHand().getType() != Material.MOB_SPAWNER) && block.getType().equals(UMaterial.SPAWNER.getMaterial()) && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand().equals(EquipmentSlot.HAND)) {
            EntityType entity = ((CreatureSpawner) block.getState()).getSpawnedType();
            String name = entity.name();
            StackableSpawner stack = Main.getStackableSpawnerManager().getSpawnerFromLocation(block.getLocation());
            player.sendMessage(MessageUtils.chat("&e&lSpawners&8: &fThere are <amount> <entitytype> Spawners".replaceAll("<amount>", String.valueOf(stack.getAmount())).replaceAll("<entitytype>", MessageUtils.getFormattedType(entity.name()))));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onSpawnerPlace(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand() != null && player.getItemInHand().getType() == Material.MOB_SPAWNER) {
            ItemStack handItem = player.getItemInHand();
            EntityType handType = ItemBuilder.getSpawnerItemType(handItem);

            if (block.getType().equals(Material.MOB_SPAWNER)) {
                StackableSpawner spawnerStack = Main.getStackableSpawnerManager().getSpawnerFromLocation(block.getLocation());
                if (spawnerStack.getEntityType() == handType && (MiscUtils.isPlayerOnIsland(player) || SuperiorSkyblockAPI.getIslandAt(player.getLocation()) == null) || SuperiorSkyblockAPI.getIslandAt(player.getLocation()).isSpawn()) {
                    event.setCancelled(true);
                    if (player.isSneaking()) {
                        int totalAmount = 1;
                        // get amount of said spawner in players inventory
                        int amount = getAmountOfSpawnersInInventory(player, handType);
                        // add and if overflow give back spawners
                        int ogAmount = spawnerStack.getAmount();

                        totalAmount = ogAmount + amount;

                        if (totalAmount == 64) {
                            player.playSound(block.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3.0F, 0.5F);
                        } else if (totalAmount > 64) {
                            List<ItemStack> spawners = ItemBuilder.getSpawnerItems(handType, totalAmount - 64);
                            ItemStack[] items = new ItemStack[spawners.size()];
                            for (int i = 0; i < items.length; i++)
                                items[i] = spawners.get(i);
                            player.getInventory().addItem(items);
                            totalAmount = 64;

                            if (ogAmount < 64)
                                player.playSound(block.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3.0F, 0.5F);

                        }
                        int finalTotalAmount = totalAmount;

                        spawnerStack.setAmount(finalTotalAmount);

                        Main.getInstance().getNMSSpawners().updateStackedSpawner(spawnerStack);

                        SpawnerPlaceEvent spe = new SpawnerPlaceEvent(player, block, handType, amount);
                        Bukkit.getPluginManager().callEvent(spe);

                        return;
                    } else if (spawnerStack.getAmount() + 1 <= 64) {
                        spawnerStack.setAmount(spawnerStack.getAmount() + 1);
                        handItem.setAmount(handItem.getAmount() - 1);

                        if (spawnerStack.getAmount() + 1 == 64)
                            player.playSound(block.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3.0F, 0.5F);

                        Main.getInstance().getNMSSpawners().updateStackedSpawner(spawnerStack);

                        SpawnerPlaceEvent spe = new SpawnerPlaceEvent(player, block, handType, 1);
                        Bukkit.getPluginManager().callEvent(spe);
                        return;
                    }
                }
            }
            Block newSpawner = block.getRelative(event.getBlockFace());
            if (newSpawner.getType() == Material.AIR && !MiscUtils.interactableBlockList().contains(block.getType()) && canPlaceSpawner(block, handType)
                    && (MiscUtils.isPlayerOnIsland(player) || player.isOp())) {
                newSpawner.setType(Material.MOB_SPAWNER);
                CreatureSpawner cs = (CreatureSpawner) newSpawner.getState();
                cs.setSpawnedType(handType);
                cs.setSpawnCount(3);
                cs.update();
                handItem.setAmount(handItem.getAmount() - 1);

                StackableSpawner spawner = Main.getStackableSpawnerManager().getSpawnerFromLocation(newSpawner.getLocation());
                Main.getInstance().getNMSSpawners().updateStackedSpawner(spawner);

                SpawnerPlaceEvent spe = new SpawnerPlaceEvent(player, newSpawner, handType, 1);
                Bukkit.getPluginManager().callEvent(spe);
            } else {
                event.setCancelled(true);
                if (!canPlaceSpawner(block, handType))
                    player.sendMessage(MessageUtils.chat("&cYou cannot place a spawner within 5 blocks of another one!"));
                else if (!MiscUtils.isPlayerOnIsland(player))
                    player.sendMessage(MessageUtils.chat("&cYou can only place spawners on your island!"));
            }
        }
    }

    @EventHandler
    public static void onSpawnerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (event.isCancelled())
            return;

        if (block.getType().equals(Material.MOB_SPAWNER)) {

            if (player.getGameMode() == GameMode.CREATIVE) {
                if (player.getItemInHand() != null) {
                    if (MiscUtils.swordList().contains(player.getItemInHand().getType())) {
                        event.setCancelled(true);
                        player.sendMessage(MessageUtils.chat("&cYou must be in survival mode to mine this spawner with a sword!"));
                        return;
                    }
                }
            }

            if (!MiscUtils.isPlayerOnIsland(player) && !player.isOp()) {
                event.setCancelled(true);
                return;
            }

            StackableSpawner spawner = Main.getStackableSpawnerManager().getSpawnerFromLocation(block.getLocation());
            EntityType spawnerType = spawner.getEntityType();
            int amount = 1;
            event.setExpToDrop(0);
            if (player.isSneaking()) {
                Main.getStackableSpawnerManager().getStackableSpawnerList().remove(spawner);
                amount = spawner.getAmount();
            } else {
                if (spawner.getAmount() > 1) {
                    event.setCancelled(true);
                    spawner.setAmount(spawner.getAmount() - 1);
                } else {
                    Main.getStackableSpawnerManager().getStackableSpawnerList().remove(spawner);
                }
            }
            if (player.getInventory().firstEmpty() != -1)
                player.getInventory().addItem(ItemBuilder.getSpawnerItem(spawnerType, amount));
            else
                spawner.getWorld().dropItemNaturally(spawner.getLocation(), ItemBuilder.getSpawnerItem(spawnerType, amount));
            SpawnerBreakEvent sbe = new SpawnerBreakEvent(player, block, spawnerType, amount);
            Bukkit.getPluginManager().callEvent(sbe);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (Main.loadedData && e.getChunk() != null && e.getChunk().isLoaded())
            loadSpawners(e.getChunk());
    }

    private static int getAmountOfSpawnersInInventory(Player player, EntityType type) {
        int count = 0;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null) {
                if (itemStack.getType() == Material.MOB_SPAWNER) {
                    if (ItemBuilder.getSpawnerItemType(itemStack) == type) {
                        count += itemStack.getAmount();
                        player.getInventory().remove(itemStack);
                    }
                }
            }
        }
        return count;
    }

    public static void loadSpawners(Chunk chunk) {
        if (chunk.getTileEntities() != null)
        Arrays.stream(chunk.getTileEntities()).filter(blockState -> blockState instanceof CreatureSpawner)
                .forEach(blockState -> Main.getInstance().getNMSSpawners().updateStackedSpawner(Main.getStackableSpawnerManager().getSpawnerFromLocation(blockState.getLocation())));
    }

    private static boolean canPlaceSpawner(Block clickedBlock, EntityType ogEntity) {
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        Block possibleSpawner = clickedBlock.getLocation().add(x, y, z).getBlock();
                        if (possibleSpawner != null && possibleSpawner.getType() != Material.AIR && possibleSpawner.getType() == Material.MOB_SPAWNER) {
                            EntityType handEntity = ((CreatureSpawner) possibleSpawner.getState()).getSpawnedType();
                            StackableSpawner spawner = Main.getStackableSpawnerManager().getSpawnerFromLocation(possibleSpawner.getLocation());
                            if (spawner != null && handEntity != null && handEntity == ogEntity) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static Location getLocationFromSpawnerDebugItem(ItemStack itemStack) {
        NBT nbt = NBT.get(itemStack);
        double x = nbt.getInt("x") + 0.5;
        int y = nbt.getInt("y");
        double z = nbt.getInt("z") + 0.5;
        World world = Bukkit.getWorld(nbt.getString("world"));
        return new Location(world, x, y, z);
    }

    private int getPage(ItemStack itemStack) {
        NBT nbt = NBT.get(itemStack);
        return nbt.getInt("spawnerPage");
    }

    private EntityType getSpawnerType(ItemStack itemStack) {
        NBT nbt = NBT.get(itemStack);
        return EntityType.valueOf(nbt.getString("spawnerType"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent ev) {
        if (ev.getChangedType().equals(Material.DRAGON_EGG)) {
            ev.setCancelled(true);
        }
    }


}
