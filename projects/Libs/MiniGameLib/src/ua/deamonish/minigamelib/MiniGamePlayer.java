package ua.deamonish.minigamelib;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;

/**
 * Класс представляющий игрока миниигры.
 */
public abstract class MiniGamePlayer {

    protected Player player;
    protected MiniGame miniGame;
    protected HashMap<String, Object> metadata = new HashMap<>();

    public MiniGamePlayer(Player player, MiniGame miniGame) {
        this.player = player;
        this.miniGame = miniGame;
        MiniGameManager.getInstance().getPlayers().put(player.getName(), this);
    }

    public MiniGamePlayer(Player player) {
        this.player = player;
        MiniGameManager.getInstance().getPlayers().put(player.getName(), this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) throws ClassCastException {
        return (T) metadata.get(key);
    }

    public void setMetadata(String key, Object obj) {
        metadata.put(key, obj);
    }

    /**
     * Что происходит, когда погибает игрок.
     */
    public abstract void death(PlayerDeathEvent event);

    public boolean hasMetadata(String key) {
        return this.metadata.containsKey(key);
    }

    public void removeMetadata(String key) {
        metadata.remove(key);
    }

    public Player getPlayer() {
        return player;
    }

    public MiniGame getMiniGame() {
        return miniGame;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setMiniGame(MiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public void sendMessage(String message){
        player.sendMessage(message);
    }
}
