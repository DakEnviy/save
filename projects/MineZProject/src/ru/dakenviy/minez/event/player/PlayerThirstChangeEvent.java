package ru.dakenviy.minez.event.player;

import org.bukkit.event.HandlerList;
import ru.dakenviy.minez.player.MineZPlayer;

public class PlayerThirstChangeEvent extends MineZPlayerEvent {

    private final static HandlerList handlers = new HandlerList();
    private boolean update;
    private int value;

    /**
     * Эвент вызывается, при изменении состояния жажды.
     *
     * @param value  - на сколько изменилась жажда.
     * @param player - Игрок
     * @param update - false - отнялось от счетчика жажды, true - добавилось к жажде.
     */
    public PlayerThirstChangeEvent(MineZPlayer player, int value, boolean update) {
        super(player);
        this.value = value;
        this.update = update;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
