package net.wechandoit.src.entity;

import net.wechandoit.src.utils.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Drops {
    private ItemStack item;
    private int minAmount;
    private int maxAmount;
    private double chance;
    private boolean cooked;
    public Drops(ItemStack item, int minAmount, int maxAmount, double chance, boolean cooked) {
        this.item = item;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.chance = chance;
        this.cooked = cooked;
    }

    public boolean isCooked() {
        return cooked;
    }

    public void setCooked(boolean cooked) {
        this.cooked = cooked;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack getDrop(boolean isCooked, int lootingLevel) {
        Random random = new Random();
        ItemStack drop = item.clone();
        if (isCooked)
            drop.setType(ItemBuilder.getCookedMaterial(drop.getType()));
        int amount = minAmount;
        if (minAmount != maxAmount + lootingLevel)
            amount = random.nextInt((maxAmount + lootingLevel) - minAmount) + minAmount;
        if (amount > 0) {
            drop.setAmount(amount);
            return drop;
        } else
            return null;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }
}
