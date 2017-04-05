package ru.dakenviy.minez.containers;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MineZChest {
    private Random random = new Random();
    private Location loc;
    private List<ChestItem> defaultItemPull = new ArrayList<>();
    private Chest chestBlock;
    private int minItemPullSize;
    private int maxItemPullSize;
    private Inventory chestInventory;

    /**
     * Создает сундук для миниигры.
     *
     * @param loc             - локация сундука.
     * @param defaultItemPull - дефолтный итем пул, откуда будут выбиратся предметы для сундука.
     * @param minItemPullSize - минимальное кол-во предметов, что будут появлятся в сундуке.
     * @param maxItemPullSize - максимальное кол-во предметов, что будут появлятся в сундуке.
     * @throws IllegalArgumentException - если локация указывает НЕ на блок сундука.
     */
    public MineZChest(Location loc, Chest chestBlock, List<ChestItem> defaultItemPull, int minItemPullSize, int maxItemPullSize) throws IllegalArgumentException {
        this.loc = loc;
        this.chestBlock = chestBlock;
        this.minItemPullSize = minItemPullSize;
        this.maxItemPullSize = maxItemPullSize;
        chestInventory = chestBlock.getBlockInventory();
        this.defaultItemPull = defaultItemPull;
    }

    /**
     * Метод, что генерирует содержимое для сундука. Отчищает сундук, заполняя его рандомным кол-вом предметов.
     */
    public void generateLoot() {
        int itemPullSize = (int) ((Math.random() * maxItemPullSize) + minItemPullSize);

        List<ItemStack> randomList = new ArrayList<>();
        while (randomList.size() != itemPullSize) {
            int percent = random.nextInt(100);
            for (ChestItem item : getDefaultItemPull()) {
                if (percent <= item.getChance() && randomList.size() < itemPullSize) {
                    randomList.add(item.getStack());
                }
            }
        }
        chestInventory.clear();
        randomList.forEach(this::tryToSetItem);
    }

    /**
     * Пытается найти рандомный свободный слот для предмета, и его всунуть, рекурсивный.
     *
     * @param stack - предмет, который хотим вставить.
     * @throws RuntimeException - если инвентарь в который мы пытаемся засунуть предмет, не имеет свободных слотов.
     */
    private void tryToSetItem(ItemStack stack) throws RuntimeException {
        int pos = random.nextInt(27);
        if (chestInventory.getItem(pos) == null) {
            chestInventory.setItem(pos, stack);
        } else {
            tryToSetItem(stack);
        }
       /* if(chestInventory.getStorageContents().length == 27){
            throw new RuntimeException("Попытка рекурсивно добавить предмет, в инвентарь полностью заполненого сундука.");
        }*/
    }

    public Location getLoc() {
        return loc;
    }

    public List<ChestItem> getDefaultItemPull() {
        return defaultItemPull;
    }

    public Chest getChestBlock() {
        return chestBlock;
    }

    public int getMaxItemPullSize() {
        return maxItemPullSize;
    }

    public void setMaxItemPullSize(int maxItemPullSize) {
        this.maxItemPullSize = maxItemPullSize;
    }

    public int getMinItemPullSize() {
        return minItemPullSize;
    }

    public void setMinItemPullSize(int minItemPullSize) {
        this.minItemPullSize = minItemPullSize;
    }


    public Inventory getChestInventory() {
        return chestInventory;
    }

    public void setChestInventory(Inventory chestInventory) {
        this.chestInventory = chestInventory;
    }

}
