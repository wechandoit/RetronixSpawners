package net.wechandoit.src.config;

import net.wechandoit.src.entity.Drops;
import net.wechandoit.src.gui.GUIButton;
import net.wechandoit.src.gui.GUIFillerItem;
import net.wechandoit.src.gui.ShopItem;
import net.wechandoit.src.utils.ItemBuilder;
import net.wechandoit.src.utils.MessageUtils;
import net.wechandoit.src.utils.UMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigHandler {

    private File file;
    private YamlConfiguration data;

    public ConfigHandler(File file) {
        this.file = file;
        this.data = YamlConfiguration.loadConfiguration(file);
        retrieveData();
    }


    public void retrieveData() {

        if (data.getConfigurationSection("mobDrops").getKeys(false) != null) {
            for (String entityType : data.getConfigurationSection("mobDrops").getKeys(false)) {
                EntityType entity = EntityType.valueOf(entityType);
                List<Drops> mobDrops = new ArrayList<Drops>();
                for (String item : data.getConfigurationSection("mobDrops." + entityType).getKeys(false)) {
                    Material material = Material.valueOf(data.getString("mobDrops." + entityType + "." + item + ".material"));
                    String amountLine = data.getString("mobDrops." + entityType + "." + item + ".amount");
                    int min = Integer.parseInt(amountLine.split("-")[0]);
                    int max = Integer.parseInt(amountLine.split("-")[1]);
                    double chance = data.getDouble("mobDrops." + entityType + "." + item + ".chance");
                    boolean cooked = data.getBoolean("mobDrops." + entityType + "." + item + ".canBeCooked");
                    int variation = data.getInt("mobDrops." + entityType + "." + item + ".data");
                    Drops mobDrop = new Drops(new ItemStack(material, 1, (short) variation), min, max, chance, cooked);
                    mobDrops.add(mobDrop);
                }
                ConfigData.mobDropMap.put(entity, mobDrops);
            }
        }


        if (data.getConfigurationSection("xP").getKeys(false) != null) {
            for (String entityType : data.getConfigurationSection("xP").getKeys(false)) {
                EntityType entity = EntityType.valueOf(entityType);
                String amountLine = data.getString("xP." + entityType);
                int min = Integer.parseInt(amountLine.split("-")[0]);
                int max = Integer.parseInt(amountLine.split("-")[1]);
                List<Integer> minMax = Arrays.asList(min, max);
                ConfigData.mobXPMap.put(entity, minMax);
            }

        }

        if (data.getConfigurationSection("spawnerValues").getKeys(false) != null) {
            for (String entityType : data.getConfigurationSection("spawnerValues").getKeys(false)) {
                EntityType entity = EntityType.valueOf(entityType);
                Double islandValue = data.getDouble("spawnerValues." + entityType);
                ConfigData.mobIslandLevelMap.put(entity, islandValue);
            }

        }

        ConfigData.shopFillerItems = new ArrayList<>();
        ConfigData.shopSpawnerItems = new ArrayList<>();

        if (data.getConfigurationSection("spawnerGUI.buttons") != null) {
            for (String icon : data.getConfigurationSection("spawnerGUI.buttons").getKeys(false)) {
                ItemStack item = ItemBuilder.getItemStackFromConfig(data, "spawnerGUI.buttons." + icon);
                List<Integer> slots = data.getIntegerList("spawnerGUI.buttons." + icon + ".slots");
                String function = data.getString("spawnerGUI.buttons." + icon + ".buttonType");

                ConfigData.spawnerGUIButtonItems.add(new GUIButton(item, slots, function));
            }
        }

        if (data.getConfigurationSection("shop.spawners") != null) {
            for (String icon : data.getConfigurationSection("shop.spawners").getKeys(false)) {
                List<Integer> slots = data.getIntegerList("shop.spawners." + icon + ".slots");
                int price = data.getInt("shop.spawners." + icon + ".price");
                EntityType type = EntityType.valueOf(icon);
                ItemStack item;
                if (type != null)
                    item = ItemBuilder.getEntitySkullItem(type);
                else
                    item = ItemBuilder.getItemStack(UMaterial.BARRIER.getItemStack(), MessageUtils.chat("&cInvalid Mob!"));

                ConfigData.shopSpawnerItems.add(new ShopItem(item, slots, price, type));
                ConfigData.mobToPriceMap.put(type, price);
            }
        }

        if (data.getConfigurationSection("shop.filleritems") != null) {
            for (String icon : data.getConfigurationSection("shop.filleritems").getKeys(false)) {
                ItemStack item = ItemBuilder.getItemStackFromConfig(data, "shop.filleritems." + icon);
                List<Integer> slots = data.getIntegerList("shop.filleritems." + icon + ".slots");

                ConfigData.shopFillerItems.add(new GUIFillerItem(item, slots));
            }


        }
    }
}
