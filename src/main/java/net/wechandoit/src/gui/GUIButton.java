package net.wechandoit.src.gui;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIButton extends GUIItem{
    private String function = "";

    public GUIButton(ItemStack item, List<Integer> slots, String function) {
        super(item, slots);
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
