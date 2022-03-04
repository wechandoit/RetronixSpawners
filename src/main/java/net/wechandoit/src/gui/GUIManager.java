package net.wechandoit.src.gui;

import net.wechandoit.src.Main;
import net.wechandoit.src.config.ConfigData;
import net.wechandoit.src.nbt.NBT;
import net.wechandoit.src.spawners.StackableSpawner;
import net.wechandoit.src.utils.ItemBuilder;
import net.wechandoit.src.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    public static Inventory getShopInventory()
    {
        Inventory inventory = Bukkit.createInventory(null, 36, MessageUtils.chat("&8Spawner Shop"));

        if (!ConfigData.shopFillerItems.isEmpty())
        {
            for (GUIItem filler : ConfigData.shopFillerItems)
            {
                for (int loc : filler.getSlots())
                {
                    inventory.setItem(loc, filler.getItem());
                }
            }
        }

        if (!ConfigData.shopSpawnerItems.isEmpty())
        {
            for (ShopItem button : ConfigData.shopSpawnerItems)
            {
                for (int loc : button.getSlots())
                {
                    ItemStack item = button.getItem().clone();
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add(MessageUtils.chat("&7Buy Price: &c$" + button.getPrice()));
                    lore.add(MessageUtils.chat("&7Click to buy 1!"));
                    lore.add(MessageUtils.chat("&cThis does not count towards the pass!"));
                    meta.setLore(lore);
                    item.setItemMeta(meta);

                    NBT nbt = NBT.get(item);
                    nbt.setString("spawnerType", button.getEntityType().name());
                    inventory.setItem(loc, nbt.apply(item));
                }
            }
        }

        return inventory;
    }

    public static List<Inventory> getDebugSpawnerMenu()
    {
        List<Inventory> inventories = new ArrayList<>();
        int amount = (Main.getStackableSpawnerManager().getStackableSpawnerList().size() / 45) + 1;
        if (Main.getStackableSpawnerManager().getStackableSpawnerList().size() % 45 == 0) amount--;
        int count = 0;
        for (int i = 0; i < amount; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, MessageUtils.chat("&8Spawner Debug Menu"));
            for (int j = 0; j < 45; j++) {
                try {

                    if (Main.getStackableSpawnerManager().getStackableSpawnerList().size() <= count)
                    {
                        break;
                    }

                    StackableSpawner spawner = Main.getStackableSpawnerManager().getStackableSpawnerList().get(count);
                    try {
                        ItemStack itemStack = ItemBuilder.getSkullFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGI2YmQ5NzI3YWJiNTVkNTQxNTI2NTc4OWQ0ZjI5ODQ3ODFhMzQzYzY4ZGNhZjU3ZjU1NGE1ZTlhYTFjZCJ9fX0=");
                        itemStack = ItemBuilder.getItemStack(itemStack, ItemBuilder.getSpawnerItem(spawner.getEntityType(), spawner.getAmount()).getItemMeta().getDisplayName());
                        itemStack.setAmount(spawner.getAmount());
                        ItemMeta meta = itemStack.getItemMeta();
                        List<String> lore = new ArrayList<>();
                        lore.add(MessageUtils.chat("&f(" + spawner.getLocation().getBlockX() + ", " + spawner.getLocation().getBlockY() + ", " + spawner.getLocation().getBlockZ() + ")"));
                        lore.add(MessageUtils.chat("&fAmount: " + spawner.getAmount()));
                        lore.add(MessageUtils.chat("&fWorld: " + spawner.getLocation().getWorld().getName()));
                        meta.setLore(lore);
                        itemStack.setItemMeta(meta);

                        NBT nbt = NBT.get(itemStack);
                        nbt.setInt("x", spawner.getLocation().getBlockX());
                        nbt.setInt("y", spawner.getLocation().getBlockY());
                        nbt.setInt("z", spawner.getLocation().getBlockZ());
                        nbt.setString("world", spawner.getWorld().getName());

                        itemStack = nbt.apply(itemStack);

                        inv.setItem(j, itemStack);
                        count++;

                    } catch (Exception ignored) {
                        Main.getStackableSpawnerManager().getStackableSpawnerList().remove(spawner);
                    }
                } catch (ArrayIndexOutOfBoundsException exception)
                {
                    break;
                }
            }
            if (i - 1 >= 0)
                inv.setItem(45, getArrowIcon(i, getButtonOfType("backarrow").getItem(), "backarrow"));
            if (i + 1 < amount)
                inv.setItem(45 + 8, getArrowIcon(i, getButtonOfType("nextarrow").getItem(), "nextarrow"));

            inventories.add(inv);
        }
        return inventories;
    }

    private static ItemStack getArrowIcon(int currentPage, ItemStack itemStack, String type) {
        ItemStack clone = itemStack.clone();
        ItemMeta meta = clone.getItemMeta();
        List<String> lore = new ArrayList<>();

        currentPage++;

        for (String line : meta.getLore()) {
            if (type.equalsIgnoreCase("backarrow")) {
                lore.add(line.replace("<page>", String.valueOf(currentPage - 1)));
            } else if (type.equalsIgnoreCase("nextarrow")) {
                lore.add(line.replace("<page>", String.valueOf(currentPage + 1)));
            } else {
                lore.add(line.replace("<page>", String.valueOf(currentPage)));
            }
        }

        meta.setLore(lore);
        clone.setItemMeta(meta);

        NBT nbt = NBT.get(clone);
        if (type.equalsIgnoreCase("backarrow")) {
            nbt.setInt("spawnerPage", currentPage - 1);
        } else if (type.equalsIgnoreCase("nextarrow")) {
            nbt.setInt("spawnerPage", currentPage + 1);
        } else {
            nbt.setInt("spawnerPage", currentPage);
        }

        return nbt.apply(clone);
    }

    private static GUIButton getButtonOfType(String type) {
        for (GUIButton button : ConfigData.spawnerGUIButtonItems) {
            if (button.getFunction().equalsIgnoreCase(type)) {
                return button;
            }
        }
        return null;
    }

}
