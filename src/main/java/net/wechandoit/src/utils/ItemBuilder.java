package net.wechandoit.src.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.wechandoit.src.nbt.NBT;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ItemBuilder {

    public static ItemStack getSkullFromBase64(String base64) {
        ItemStack item = UMaterial.PLAYER_HEAD_ITEM.getItemStack();
        notNull(base64, "base64");

        UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(item,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
    }

    private static void notNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException(name + " should not be null!");
        }
    }

    public static ItemStack getItemStack(Material material, int amount, short data) {
        return new ItemStack(material, amount, data);
    }

    public static ItemStack getItemStack(Material material, int amount, short data, String name) {
        ItemStack itemStack = new ItemStack(material, amount, data);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(MessageUtils.chat(name));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getItemStack(Material material, int amount, short data, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(material, amount, data);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getItemStack(ItemStack itemStack, String name, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getItemStack(ItemStack itemStack, String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getSkullFromName(String name) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        notNull(name, "name");

        return Bukkit.getUnsafe().modifyItemStack(item,
                "{SkullOwner:\"" + name + "\"}"
        );
    }

    public static ItemStack getSkullFromURL(String url) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (url == null || url.isEmpty())
            return skull;
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public static ItemStack getItemStack(ItemStack itemStack, String name, String NBTname, int value) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);
        NBT nbt = NBT.get(itemStack);
        nbt.setInt(NBTname, value);
        return nbt.apply(itemStack);
    }

    public static Material getCookedMaterial(Material material) {
        switch (material) {
            case PORK:
                return Material.GRILLED_PORK;
            case RAW_BEEF:
                return Material.COOKED_BEEF;
            case MUTTON:
                return Material.COOKED_MUTTON;
            case RABBIT:
                return Material.COOKED_RABBIT;
            case RAW_CHICKEN:
                return Material.COOKED_CHICKEN;
            default:
                return material;
        }
    }

    public static ItemStack getSpawnerItem(EntityType entityType, int amount) {
        ItemStack itemStack = getItemStack(UMaterial.SPAWNER.getItemStack(), MessageUtils.chat("&e<entitytype> &fSpawner").replace("<amount>", String.valueOf(amount)).replace("<entitytype>", MessageUtils.getFormattedType(entityType.name())));
        ItemMeta meta = itemStack.getItemMeta();
        CreatureSpawner cs = (CreatureSpawner) ((BlockStateMeta) meta).getBlockState();
        cs.setSpawnedType(entityType);
        ((BlockStateMeta) meta).setBlockState(cs);
        itemStack.setItemMeta(meta);
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static List<ItemStack> getSpawnerItems(EntityType entityType, int amount) {
        List<ItemStack> itemStacks = new ArrayList<>();
        while (amount / 64 > 0) {
            ItemStack item = getSpawnerItem(entityType, 64);
            itemStacks.add(item);
            amount -= 64;
        }
        if (amount > 0) {
            ItemStack item = getSpawnerItem(entityType, amount);
            itemStacks.add(item);
        }
        return itemStacks;
    }

    public static ItemStack getEntitySkullItem(EntityType entityType)
    {
        if (getSkullOfEntity(entityType) != null)
        {
            return getItemStack(getSkullOfEntity(entityType), MessageUtils.chat(MessageUtils.chat("&e<entitytype> &fSpawner").replace("<entitytype>", MessageUtils.getFormattedType(entityType.name()))));
        }
        return null;
    }

    public static EntityType getSpawnerItemType(ItemStack itemStack) {
        if(itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        if(!(meta instanceof BlockStateMeta)) return null;

        BlockStateMeta stateMeta = (BlockStateMeta) meta;
        BlockState state = stateMeta.getBlockState();
        if(!(state instanceof CreatureSpawner)) return null;

        CreatureSpawner spawner = (CreatureSpawner) state;
        return spawner.getSpawnedType();
    }

    public static ItemStack getItemStackFromConfig(YamlConfiguration config, String path) {
        Material material = Material.getMaterial(config.getString(path + ".material"));
        if (material == null) material = Material.BARRIER;
        String name = MessageUtils.chat(config.getString(path + ".name"));
        int amount = config.getInt(path + ".amount");
        int damage = config.getInt(path + ".data");
        List<String> lore = new ArrayList<>();
        if (config.getStringList(path + ".lore") != null) {
            for (String line : config.getStringList(path + ".lore"))
                lore.add(MessageUtils.chat(line));
        }
        String username = "";
        if (config.getString(path + ".username") != null) username = config.getString(path + ".username");
        String url = "";
        if (config.getString(path + ".url") != null) url = config.getString(path + ".url");
        String base64 = "";
        if (config.getString(path + ".base64") != null) base64 = config.getString(path + ".base64");
        boolean hideEnchants = config.getBoolean(path + ".hideVanillaEnchants");
        if (material == Material.SKULL_ITEM && damage == 3) {
            if (!url.equals(""))
                return getItemStack(getSkullFromURL(url), name, lore);
            else if (!username.equals(""))
                return getItemStack(getSkullFromName(username), name, lore);
            else if (!base64.equals(""))
                return getItemStack(getSkullFromBase64(base64), name, lore);
            else
                return getItemStack(material, amount, (short) damage, name, lore);
        } else {
            return getItemStack(material, amount, (short) damage, name, lore);
        }
    }

    public static ItemStack getSkullOfEntity(EntityType entityType)
    {
        switch (entityType)
        {
            case RABBIT:
                return ItemBuilder.getSkullFromName("MHF_Rabbit");
            case COW:
                return ItemBuilder.getSkullFromName("MHF_Cow");
            case PIG:
                return ItemBuilder.getSkullFromName("MHF_Pig");
            case CHICKEN:
                return ItemBuilder.getSkullFromName("MHF_Chicken");
            case SHEEP:
                return ItemBuilder.getSkullFromName("MHF_Sheep");
            case PIG_ZOMBIE:
                return ItemBuilder.getSkullFromName("MHF_PigZombie");
            case CREEPER:
                return ItemBuilder.getSkullFromName("MHF_Creeper");
            case SPIDER:
                return ItemBuilder.getSkullFromName("MHF_Spider");
            case ZOMBIE:
                return ItemBuilder.getSkullFromName("MHF_Zombie");
            case ENDERMAN:
                return ItemBuilder.getSkullFromName("MHF_Enderman");
            case SKELETON:
                return ItemBuilder.getSkullFromName("MHF_Skeleton");
            case WITCH:
                return ItemBuilder.getSkullFromName("MHF_Witch");
            case BLAZE:
                return ItemBuilder.getSkullFromName("MHF_Blaze");
            case IRON_GOLEM:
                return ItemBuilder.getSkullFromName("MHF_Golem");
            default:
                return null;
        }
    }

}
