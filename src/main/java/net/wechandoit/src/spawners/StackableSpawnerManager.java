package net.wechandoit.src.spawners;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.spawner.SpawnerStack;
import net.wechandoit.src.Main;
import net.wechandoit.src.utils.Java9Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StackableSpawnerManager {
    private List<StackableSpawner> stackableSpawnerList;
    private File file;
    private YamlConfiguration data;

    public StackableSpawnerManager(File file) {
        stackableSpawnerList = new ArrayList<>();
        this.file = file;
        this.data = YamlConfiguration.loadConfiguration(file);
        retrieveData();
    }

    public List<StackableSpawner> getStackableSpawnerList() {
        return stackableSpawnerList;
    }

    public void setStackableSpawnerList(List<StackableSpawner> stackableSpawnerList) {
        this.stackableSpawnerList = stackableSpawnerList;
    }

    public void retrieveData() {

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        Date date = new Date();

        if (data.getConfigurationSection("spawners") == null) {
            return;
        }
        for (String uuid : data.getConfigurationSection("spawners").getKeys(false)) {
            try {
                double locX = data.getDouble("spawners." + uuid + ".locx");
                double locY = data.getDouble("spawners." + uuid + ".locy");
                double locZ = data.getDouble("spawners." + uuid + ".locz");
                World world = Bukkit.getWorld(data.getString("spawners." + uuid + ".world"));
                int amount = data.getInt("spawners." + uuid + ".amount");
                UUID uniqueId = Java9Utils.getUUIDfromString(uuid);
                StackableSpawner spawner = new StackableSpawner(amount, new Location(world, locX, locY, locZ));
                spawner.setUniqueId(uniqueId);
                stackableSpawnerList.add(spawner);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                continue;
            }
        }

        saveData(new File(Main.getInstance().parentPath + "backup/", "backup" + formatter.format(date) + ".yml"));

        System.out.println(stackableSpawnerList.size() + " spawners loaded!");
    }

    public boolean saveData(File file) {
        data.set("spawners", new ArrayList<String>());
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (stackableSpawnerList == null || stackableSpawnerList.isEmpty()) {
            return false;
        }
        for (StackableSpawner spawner : stackableSpawnerList) {
            data.set("spawners." + spawner.getUniqueId().toString() + ".locx", spawner.getLocation().getBlockX());
            data.set("spawners." + spawner.getUniqueId().toString() + ".locy", spawner.getLocation().getBlockY());
            data.set("spawners." + spawner.getUniqueId().toString() + ".locz", spawner.getLocation().getBlockZ());
            data.set("spawners." + spawner.getUniqueId().toString() + ".world", (spawner.getWorld().getName()));
            data.set("spawners." + spawner.getUniqueId().toString() + ".amount", spawner.getAmount());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("[RetronixSpawners] Saved " + stackableSpawnerList.size() + " spawners!");
        return true;
    }

    public StackableSpawner getSpawnerFromLocation(Location location)
    {
        if (stackableSpawnerList.isEmpty())
        {
            if (location.getBlock().getType() == Material.MOB_SPAWNER)
                return createSpawnerFromLocation(location);
            return null;
        }
        for (StackableSpawner s : stackableSpawnerList)
        {
            if (s.getLocation().equals(location))
                return s;
        }
        if (location.getBlock().getType() == Material.MOB_SPAWNER)
            return createSpawnerFromLocation(location);
        return null;
    }

    public StackableSpawner createSpawnerFromLocation(Location location)
    {
        StackableSpawner s = new StackableSpawner(1, location);
        stackableSpawnerList.add(s);
        return s;
    }

    public void convertSpawners(String pluginName)
    {
        if (pluginName.equalsIgnoreCase("UltimateStacker"))
        {
            Collection<SpawnerStack> usStacks = UltimateStacker.getInstance().getSpawnerStackManager().getStacks();
            int count = 0;
            for (SpawnerStack spawnerStack : usStacks) {
                Block block = spawnerStack.getLocation().getBlock();
                if (block.getType().equals(Material.MOB_SPAWNER)) {
                    StackableSpawner s = new StackableSpawner(spawnerStack.getAmount(), spawnerStack.getLocation());
                    stackableSpawnerList.add(s);
                    count++;
                }
            }
            System.out.println("USCONVERTER: " + count + " spawners converted!");
        } else if (pluginName.equalsIgnoreCase("WildStacker")) {
            List<StackedSpawner> wsStacks = WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawners();
            int count = 0;
            for (StackedSpawner spawnerStack : wsStacks) {
                Block block = spawnerStack.getLocation().getBlock();
                if (block.getType().equals(Material.MOB_SPAWNER)) {
                    StackableSpawner s = new StackableSpawner(spawnerStack.getStackAmount(), spawnerStack.getLocation());
                    stackableSpawnerList.add(s);
                    count++;
                }
            }
            System.out.println("WSCONVERTER: " + count + " spawners converted!");
        }
    }
}
