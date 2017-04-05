package ru.dakenviy.minez.containers;

import org.bukkit.inventory.ItemStack;

public class ChestItem {

    private int chance;
    private ItemStack stack;

    public ChestItem(int chance, ItemStack stack) {
        this.chance = chance;
        this.stack = stack;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public ItemStack getStack() {
        return stack.clone();
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
}
