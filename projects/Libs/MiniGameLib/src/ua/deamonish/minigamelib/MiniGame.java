package ua.deamonish.minigamelib;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import ua.deamonish.minigamelib.events.MiniGameJoinEvent;
import ua.deamonish.minigamelib.events.MiniGameQuitEvent;

import java.util.HashMap;
import java.util.Random;

/**
 * Класс представляющий собой абстракцию миниигры.
 */
public abstract class MiniGame {

    protected World world;
    protected int maxPlayerSize = 100; // Дефолтное значение.
    protected String miniGameName;
    protected HashMap<String, MiniGamePlayer> players = new HashMap<>();
    private MiniGameLogger logger;
    private Plugin plugin;

    public MiniGame(String miniGameName, int maxPlayerSize, Plugin plugin) {
        this.miniGameName = miniGameName;
        this.maxPlayerSize = maxPlayerSize;
        this.world = Bukkit.getWorld(miniGameName);
        this.plugin = plugin;
        this.logger = new MiniGameLogger(plugin, this);
        if (world == null) {
            createWorld();
        }
        // Убираем автосейв.
        world.setAutoSave(false);
    }

    public MiniGame(String miniGameName, Plugin plugin) {
        this.miniGameName = miniGameName;
        this.plugin = plugin;
        this.world = Bukkit.getWorld(miniGameName);
        this.logger = new MiniGameLogger(plugin, this);
        if (world == null) {
            createWorld();
        }
        world.setAutoSave(false);
    }

    private void createEmptyWorld() {
        ChunkGenerator chunkGenerator = new ChunkGenerator() {
            @Override
            public byte[] generate(World world, Random random, int cx, int cz) {
                return new byte[32768];
            }
        };
        this.world = WorldCreator.name(miniGameName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generator(chunkGenerator).createWorld();
        if (world == null) {
            throw new RuntimeException("Мир '" + miniGameName + "' не найден и не был создан.");
        }
    }

    private void createWorld() {
        this.world = Bukkit.createWorld(new WorldCreator(miniGameName).type(WorldType.FLAT).environment(World.Environment.NORMAL));
        if (world == null) {
            throw new RuntimeException("Мир '" + miniGameName + "' не найден и не был создан.");
        }
    }

    public void broadcastMessage(String message) {
        players.values().forEach(p -> p.sendMessage(message));
    }

    /**
     * Добавить игрока в миниигру.
     *
     * @param player - объект игрока, которого мы хотим добавить.
     */
    public void addPlayer(MiniGamePlayer player) {
        players.put(player.getPlayer().getName(), player);
        player.setMiniGame(this);
        Bukkit.getPluginManager().callEvent(new MiniGameJoinEvent(player, this));
    }

    public MiniGamePlayer getPlayer(String name) {
        return players.get(name);
    }

    public MiniGamePlayer getPlayer(Player player) {
        return players.get(player.getName());
    }

    /**
     * Удалить игрока из миниигры.
     *
     * @param playerName - имя игрока, которого мы хотим удалить.
     */
    public void removePlayer(String playerName) {
        MiniGamePlayer player = players.remove(playerName);
        if (player != null) {
            Bukkit.getPluginManager().callEvent(new MiniGameQuitEvent(player, this));
        }
    }

    /**
     * Удалить игрока из миниигры.
     *
     * @param player - игрок, которого мы хотим удалить.
     */
    public void removePlayer(MiniGamePlayer player) {
        if (player != null) {
            players.remove(player.getPlayer().getName());
            Bukkit.getPluginManager().callEvent(new MiniGameQuitEvent(player, this));
        }
    }

    /**
     * Возвращает мир, в котором происходит миниигра.
     *
     * @return - World миниигры.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Получить список игроков.
     *
     * @return - список игроков, миниигры, в этом мире.
     */
    public HashMap<String, MiniGamePlayer> getPlayers() {
        return players;
    }

    public String getMiniGameName() {
        return miniGameName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MiniGame other = (MiniGame) obj;
        return getMiniGameName().equals(other.getMiniGameName());
    }

    @Override
    public String toString() {
        return miniGameName;
    }

    public int getOnline() {
        return players.size();
    }

    public int getMaxPlayerSize() {
        return maxPlayerSize;
    }

    protected Plugin getPlugin() {
        return plugin;
    }

    public MiniGameLogger getLogger() {
        return logger;
    }
}
