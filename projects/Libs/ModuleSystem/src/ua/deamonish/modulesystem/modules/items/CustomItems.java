package ua.deamonish.modulesystem.modules.items;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CustomItems {

    private static HashMap<String, ItemStack> customItems = new HashMap<>();

    public static void addItem(String key, ItemStack itemStack){
        customItems.put(key, itemStack);
    }

    public static void removeItem(String key){
        customItems.remove(key);
    }

    public static ItemStack getItem(String key){
       return customItems.get(key);
    }

    public static boolean isContains(String key){
        return customItems.keySet().contains(key);
    }

    public static boolean isContains(ItemStack itemStack){
        return customItems.values().contains(itemStack);
    }
}
