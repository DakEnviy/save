package ru.dakenviy.minez.containers;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum ArmorItemType {
    HAND(Arrays.asList(Material.values())),
    BOOTS(Arrays.asList(Material.LEATHER_BOOTS, Material.IRON_BOOTS, Material.CHAINMAIL_BOOTS, Material.DIAMOND_BOOTS, Material.GOLD_BOOTS)),
    LEGGINGS(Arrays.asList(Material.LEATHER_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.GOLD_LEGGINGS, Material.IRON_LEGGINGS)),
    HELMET(Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.GOLD_HELMET, Material.DIAMOND_HELMET, Material.IRON_HELMET)),
    CHESTPLATE(Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.GOLD_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE));

    private List<Material> availableMaterial;

    ArmorItemType(List<Material> availableMaterial) {
        this.availableMaterial = availableMaterial;
    }

    public List<Material> getAvailableMaterial() {
        return availableMaterial;
    }

    public void setAvailableMaterial(List<Material> availableMaterial) {
        this.availableMaterial = availableMaterial;
    }


}
