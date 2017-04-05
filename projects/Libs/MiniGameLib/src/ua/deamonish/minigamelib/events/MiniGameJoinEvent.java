package ua.deamonish.minigamelib.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ua.deamonish.minigamelib.MiniGame;
import ua.deamonish.minigamelib.MiniGamePlayer;

/**
 * Эвент вызывается, когда игрок заходит в игру.
 */
public class MiniGameJoinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private MiniGamePlayer player;
    private MiniGame miniGame;

    public MiniGameJoinEvent(MiniGamePlayer player, MiniGame miniGame) {
        this.player = player;
        this.miniGame = miniGame;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return MiniGameJoinEvent.handlers;
    }

    public MiniGamePlayer getPlayer() {
        return player;
    }

    public MiniGame getMiniGame() {
        return miniGame;
    }
}
