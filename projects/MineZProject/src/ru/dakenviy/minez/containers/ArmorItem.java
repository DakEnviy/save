package ru.dakenviy.minez.containers;

import org.bukkit.inventory.ItemStack;

public class ArmorItem extends ChestItem {

    public ArmorItem(int chance, ItemStack stack, ArmorItemType type) throws IllegalArgumentException {
        super(chance, stack);
        this.armorItemType = type;
        if (!type.getAvailableMaterial().contains(stack.getType())) {
            throw new IllegalArgumentException("Предмет для арморстенда" + stack.getType() + "/" + stack.getType().getId() + " не может находится в ячейке " + type.name());
        }
    }

    private ArmorItemType armorItemType;

    public ArmorItemType getArmorItemType() {
        return armorItemType;
    }
}
