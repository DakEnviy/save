package ua.deamonish.minigamelib;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * Класс, управляющий минииграми.
 */
@SuppressWarnings("unchecked")
public class MiniGameManager implements Listener {

    private static MiniGameManager instance = null;
    private Map<String, MiniGame> miniGames = new HashMap<>();
    private HashMap<String, MiniGamePlayer> players = new HashMap<>();
    private Random random = new Random();

    /**
     * Зарагестрировать миниигру.
     *
     * @param miniGame - минигру, что нужно зарегестрировать.
     */
    public void addMiniGame(MiniGame miniGame) {
        miniGames.put(miniGame.getMiniGameName(), miniGame);
    }

    public void removeMiniGame(MiniGame miniGame) {
        Bukkit.getServer().unloadWorld(miniGame.getWorld(), false);
        miniGames.remove(miniGame.getMiniGameName());
    }

    /**
     * Получить все доступные миниигры.
     *
     * @return - карта миниигр.
     */
    public Map<String, MiniGame> getMiniGames() {
        return miniGames;
    }

    /**
     * Метод которым можно получить множество миниигр, уже скастованые к типу <T>
     * @param <T> - наследник миниигры, к которому будем обобщать игры.
     * @return Множество, где уже скастованые элементы к типу <T>.
     */
    public <T extends MiniGame> HashSet<T> getMiniGame(){
        HashSet <T> set = new HashSet<>();
        for(MiniGame basic : miniGames.values()){
            set.add((T)basic);
        }
        return set;
    }

    public void searchMiniGame(MiniGamePlayer player) {

    }

    /**
     * Получить объект игрока миниигры.
     *
     * @param player - объект, к которому привязан MiniGamePlayer
     * @return MiniGamePlayer.
     */
    @Nullable
    public MiniGamePlayer getMineGamePlayer(Player player) {
        return players.get(player.getName());
    }


    /**
     * Получить уже скастованого игрока.
     * @param <T> тип к которому преобобщаем
     * @return возвращает уже скастованый тип.
     * @throws ClassCastException - если элементы игроков, попытаться преобобщить к неверному типу.
     */

    public <T extends MiniGamePlayer> T getMiniGamePlayer(Player player) throws ClassCastException{
        return (T) players.get(player.getName());
    }

    public <T extends MiniGame> T getMiniGame(String name) throws ClassCastException {
        return (T) miniGames.get(name);
    }

    public <T extends MiniGame> T getMiniGame(World world) throws ClassCastException {
        return (T) miniGames.get(world.getName());
    }


    /**
     * Найти игру, в которой меньше всего игроков.
     *
     * @return MiniGame с наименьшим кол-вом игроков.
     */
    public MiniGame findMiniGame() {
        int max = 0;
        // Список хороших вариантов.
        List<MiniGame> games = new ArrayList<>();
        for (MiniGame miniGame : miniGames.values()) {
            int size = miniGame.getPlayers().size();
            if (size >= miniGame.getMaxPlayerSize()) {
                continue;
            }

            if (size == max) {
                games.add(miniGame);
            } else if (size > max) {
                max = size;
                games.clear();
                games.add(miniGame);
            }
        }

        if (games.isEmpty()) {
            return null;
        }
        // Если размер 1. Нет нужды прогонять рандом.
        if (games.size() == 1) {
            return games.get(0);
        }

        return games.get(random.nextInt(games.size()));
    }

    /**
     * Добавляет игрока в выбранную игру.
     *
     * @param player   - игрок, которого хотим добавить в миниигру.
     * @param miniGame - миниигра, в которую хотим добавить игрока.
     */
    public void addPlayerToMiniGame(MiniGamePlayer player, MiniGame miniGame) {
        miniGame.addPlayer(player);
    }

    public void removePlayerOnMiniGame(MiniGamePlayer player, MiniGame miniGame) {
        miniGame.removePlayer(player);
        player.setMiniGame(null);
    }

    /**
     * Получить менеджер, для управления минииграми.
     *
     * @return MiniGameManager.
     */
    public static MiniGameManager getInstance() {
        if (instance == null) {
            instance = new MiniGameManager();
        }
        return instance;
    }

    public HashMap<String, MiniGamePlayer> getPlayers() {
        return players;
    }

    public Collection<MiniGamePlayer> getMiniGamePlayers(){
        return players.values();
    }
}
