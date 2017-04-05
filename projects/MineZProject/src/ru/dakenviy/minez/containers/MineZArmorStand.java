package ru.dakenviy.minez.containers;

import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MineZArmorStand {

    private ArmorStand stand;
    private List<ArmorItem> itemPull = new ArrayList<>();
    private Random random = new Random();

    public MineZArmorStand(ArmorStand stand) {
        this.stand = stand;
    }

    public MineZArmorStand(ArmorStand stand, List<ArmorItem> itemPull) {
        this.stand = stand;
        this.itemPull = itemPull;
    }

    public ArmorStand getStand() {
        return stand;
    }

    public void playRandom() {
        reset();
        for (ArmorItem item : itemPull) {
            int rand = random.nextInt(100);
            if (rand <= item.getChance()) {
                setItemForType(item);
            }
        }
    }

    private void reset() {
        stand.setItemInHand(null);
        stand.setLeggings(null);
        stand.setChestplate(null);
        stand.setBoots(null);
        stand.setHelmet(null);
    }

    private void setItemForType(ArmorItem item) {
        ArmorItemType type = item.getArmorItemType();
        switch (type) {
            case BOOTS:
                stand.setBoots(item.getStack());
                return;
            case CHESTPLATE:
                stand.setChestplate(item.getStack());
                return;
            case HAND:
                stand.setItemInHand(item.getStack());
                return;
            case HELMET:
                stand.setHelmet(item.getStack());
                return;
            case LEGGINGS:
                stand.setLeggings(item.getStack());
                break;
        }
    }

    public void setStand(ArmorStand stand) {
        this.stand = stand;
    }

    public List<ArmorItem> getItemPull() {
        return itemPull;
    }

    public void setItemPull(List<ArmorItem> itemPull) {
        this.itemPull = itemPull;
    }
}
