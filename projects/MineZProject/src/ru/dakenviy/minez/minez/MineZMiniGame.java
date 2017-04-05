package ru.dakenviy.minez.minez;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import ru.dakenviy.minez.containers.MineZArmorStand;
import ru.dakenviy.minez.containers.MineZChest;
import ua.deamonish.minigamelib.MiniGame;
import ua.deamonish.minigamelib.MiniGamePlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * МиниИгра майнзет. Класс, ее реализовывающий.
 */
public class MineZMiniGame extends MiniGame {

    private List<Location> spawnPoints = new ArrayList<>();
    private Random random = new Random();
    private HashSet<MineZChest> chests = new HashSet<>();
    private HashSet<MineZArmorStand> stands = new HashSet<>();
    private List<Order> orders = new ArrayList<>();

    public MineZMiniGame(String miniGameName, int maxPlayerSize, Plugin plugin) {
        super(miniGameName, maxPlayerSize, plugin);
    }

    public void setSpawnPoint(List<Location> spawnPoints) {
        List<Location> locList = new ArrayList<>(spawnPoints.size());
        for(Location loc : spawnPoints){
            Location add = loc.clone();
            add.setWorld(getWorld());
            locList.add(add);
        }
        this.spawnPoints = locList;
    }

    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }

    public void teleportPlayerToSpawn(MiniGamePlayer player) {
        Location spawn = spawnPoints.get(random.nextInt(spawnPoints.size())).clone();
        spawn.add(random.nextDouble()*3-3, 0, random.nextDouble()*3-3);
        player.getPlayer().teleport(spawn);
    }
    public void addStand(MineZArmorStand stand){
        stands.add(stand);
    }
    public HashSet<MineZArmorStand> getStands() {
        return stands;
    }

    public void setStands(HashSet<MineZArmorStand> stands) {
        this.stands = stands;
    }

    public HashSet<MineZChest> getChests() {
        return chests;
    }

    public void setChests(HashSet<MineZChest> chests) {
        this.chests = chests;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<Order> getLimitedOrders() {
        if(orders.size() >= 10) {
            return orders.subList(0, 10);
        } else {
            return orders.subList(0, orders.size());
        }
    }
}
