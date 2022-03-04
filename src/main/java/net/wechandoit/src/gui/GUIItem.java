package net.wechandoit.src.gui;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class GUIItem {
    private ItemStack item;
    private List<Integer> slots;

    public GUIItem(ItemStack item, List<Integer> slots) {
        this.item = item;
        this.slots = slots;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

}
