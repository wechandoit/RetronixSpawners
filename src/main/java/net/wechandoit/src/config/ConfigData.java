package net.wechandoit.src.config;

import net.wechandoit.src.entity.Drops;
import net.wechandoit.src.gui.GUIButton;
import net.wechandoit.src.gui.GUIFillerItem;
import net.wechandoit.src.gui.ShopItem;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigData {
    // mob drops
    public static HashMap<EntityType, List<Drops>> mobDropMap = new HashMap<>();
    public static HashMap<EntityType, List<Integer>> mobXPMap = new HashMap<>();
    public static HashMap<EntityType, Double> mobIslandLevelMap = new HashMap<>();
    public static HashMap<EntityType, Integer> mobToPriceMap = new HashMap<>();

    public static List<GUIFillerItem> shopFillerItems;
    public static List<ShopItem> shopSpawnerItems;


    public static List<GUIButton> spawnerGUIButtonItems = new ArrayList<>();
}
