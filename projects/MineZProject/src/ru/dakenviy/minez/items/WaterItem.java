package ru.dakenviy.minez.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class WaterItem {

    private ItemStack stack;
    private int thirst = 0;

    /**
     * Предмет, что будет восстанавлиать жажду.
     *
     * @param type   - материал предмета (указывать нужно тот материал, что можно пить/есть))
     * @param thirst - кол-во восстанавливаемой жажды.
     */
    public WaterItem(Material type, int thirst) {
        stack = new ItemStack(type);
        this.thirst = thirst;
    }

    public WaterItem(ItemStack stack, int thirst) {
        this.stack = stack;
        this.thirst = thirst;
    }

    private static HashMap<String, WaterItem> availableWaterItems = new HashMap<>();

    public static HashMap<String, WaterItem> getAvailableWaterItems() {
        return availableWaterItems;
    }

    public static WaterItem getWaterItem(String key) {
        return availableWaterItems.get(key);
    }

    public static WaterItem getWaterItem(ItemStack stack) {
        for (WaterItem item : availableWaterItems.values()) {
            if (item.getItemStack().equals(stack)) {
                return item;
            }
        }
        return null;
    }

    public static void addWaterItem(String key, WaterItem item) {
        availableWaterItems.put(key, item);
    }

    public int getThirst() {
        return thirst;
    }

    public void setThirst(int thirst) {
        this.thirst = thirst;
    }

    public ItemStack getItemStack(){
        return stack;
    }

    public static boolean isContains(ItemStack stack){
        return getWaterItem(stack) != null;
    }
}
