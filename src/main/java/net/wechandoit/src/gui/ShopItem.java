package net.wechandoit.src.gui;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopItem extends GUIItem{

    private int price;
    private EntityType entityType;

    public ShopItem(ItemStack item, List<Integer> slots, int price, EntityType entityType) {
        super(item, slots);
        this.price = price;
        this.entityType = entityType;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }



}
