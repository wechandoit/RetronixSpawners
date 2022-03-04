package net.wechandoit.src;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import de.tr7zw.nbtinjector.NBTInjector;
import net.milkbowl.vault.economy.Economy;
import net.wechandoit.src.commands.RetronixSpawnerCmd;
import net.wechandoit.src.config.ConfigData;
import net.wechandoit.src.config.ConfigHandler;
import net.wechandoit.src.gui.GUIManager;
import net.wechandoit.src.listeners.EntityListeners;
import net.wechandoit.src.listeners.PlayerListeners;
import net.wechandoit.src.nms.NMSAdapter;
import net.wechandoit.src.nms.NMSSpawners;
import net.wechandoit.src.spawners.StackableSpawner;
import net.wechandoit.src.spawners.StackableSpawnerManager;
import net.wechandoit.src.spawning.SpawnCondition;
import net.wechandoit.src.utils.ReflectionUtils;
import net.wechandoit.src.utils.ServerVersion;
import net.wechandoit.src.utils.structures.FastEnumMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.api.EntityManager;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class Main extends JavaPlugin {

    private static Main plugin;
    private static StackableSpawnerManager stackableSpawnerManager;
    private static EntityManager em;
    private static ConfigHandler configHandler;
    private static Economy econ = null;
    private static Inventory shopInventory;
    public static Random random;
    public static boolean loadedData = false;

    private NMSSpawners nmsSpawners;
    private NMSAdapter nmsAdapter;

    private final FastEnumMap<EntityType, Set<SpawnCondition>> spawnConditions = new FastEnumMap<>(EntityType.class);
    private final Map<String, SpawnCondition> spawnConditionsIds = new HashMap<>();

    public String parentPath = "plugins/" + getName() + "/";

    @Override
    public void onLoad() {
        NBTInjector.inject();
    }

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();

        if (!setupEconomy()) {
            Main.getInstance().getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(Main.plugin);
            return;
        }

        random = new Random();

        ReflectionUtils.init();

        loadEvents();
        loadCommands();

        loadNMSAdapter();
        loadedData = true;

        new BukkitRunnable() {

            @Override
            public void run() {

                stackableSpawnerManager = new StackableSpawnerManager(new File(parentPath, "spawners.yml"));
                configHandler = new ConfigHandler(new File(parentPath, "config.yml"));

                shopInventory = GUIManager.getShopInventory();

                StackMob sm = (StackMob) Bukkit.getPluginManager().getPlugin("StackMob");
                em = new EntityManager(sm);

                runner();
            }
        }.runTaskLater(this, 20 * 2); // 5 second delay or the plugin freaks out

    }

    private void runner() {
        (new BukkitRunnable() {
            public void run() {
                resetBonusLevels();
                recalcIslands();
            }
        }).runTaskTimer(this, 20L, 20L);
    }

    private void resetBonusLevels() {
        for (Island island : SuperiorSkyblockAPI.getGrid().getIslands()) {
            island.setBonusLevel(new BigDecimal(0));
        }
    }

    private void recalcIslands() {
        for (StackableSpawner spawner : stackableSpawnerManager.getStackableSpawnerList()) {
            if (SuperiorSkyblockAPI.getIslandAt(spawner.getLocation()) != null && ConfigData.mobIslandLevelMap.containsKey(spawner.getEntityType())) {
                Island island = SuperiorSkyblockAPI.getIslandAt(spawner.getLocation());
                island.setBonusLevel(island.getBonusLevel().add(new BigDecimal(ConfigData.mobIslandLevelMap.get(spawner.getEntityType()) * spawner.getAmount())));
            }
        }
    }

    private void loadNMSAdapter() {
        String bukkitVersion = ServerVersion.getBukkitVersion();
        try {
            nmsAdapter = (NMSAdapter) Class.forName("net.wechandoit.src.nms.NMSAdapter_" + bukkitVersion).newInstance();
            nmsSpawners = (NMSSpawners) Class.forName("net.wechandoit.src.nms.NMSSpawners_" + bukkitVersion).newInstance();
        } catch (Exception ex) {
            System.out.println("RetronixSpawners doesn't support " + bukkitVersion + " - shutting down...");
        }
    }

    public NMSSpawners getNMSSpawners() {
        return nmsSpawners;
    }

    public NMSAdapter getNmsAdapter() {
        return nmsAdapter;
    }

    @Override
    public void onDisable() {
        stackableSpawnerManager.saveData(new File(parentPath, "spawners.yml"));
    }

    private void loadEvents() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityListeners(), this);
    }

    private void loadCommands() {
        getCommand("retronixspawners").setExecutor(new RetronixSpawnerCmd());
    }

    public static Main getInstance() {
        return plugin;
    }

    public static StackableSpawnerManager getStackableSpawnerManager() {
        return stackableSpawnerManager;
    }

    public static EntityManager getStackableEntityManager() {
        return em;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Inventory getShopInventory() {
        return shopInventory;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public void addSpawnCondition(SpawnCondition spawnCondition, EntityType... entityTypes) {
        for (EntityType entityType : entityTypes)
            spawnConditions.computeIfAbsent(entityType, new HashSet<>(1)).add(spawnCondition);
    }

    public Collection<SpawnCondition> getSpawnConditions(EntityType entityType) {
        return Collections.unmodifiableSet(spawnConditions.getOrDefault(entityType, new HashSet<>()));
    }

    public void removeSpawnCondition(EntityType entityType, SpawnCondition spawnCondition) {
        Set<SpawnCondition> spawnConditionSet = spawnConditions.get(entityType);
        if (spawnConditionSet != null)
            spawnConditionSet.remove(spawnCondition);
    }

    public void clearSpawnConditions(EntityType entityType) {
        spawnConditions.remove(entityType);
    }

    public Optional<SpawnCondition> getSpawnCondition(String id) {
        return Optional.ofNullable(spawnConditionsIds.get(id.toLowerCase()));
    }

    public SpawnCondition registerSpawnCondition(SpawnCondition spawnCondition) {
        spawnConditionsIds.put(spawnCondition.getId().toLowerCase(), spawnCondition);
        return spawnCondition;
    }
}
