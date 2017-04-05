package ru.dakenviy.minez.event.player;

import org.bukkit.event.HandlerList;
import ru.dakenviy.minez.player.MineZPlayer;
import ru.dakenviy.minez.stages.FractureStage;

public class PlayerFractureChangeEvent extends MineZPlayerEvent {

    private final static HandlerList handlers = new HandlerList();
    private FractureStage newStage;

    public PlayerFractureChangeEvent(MineZPlayer player, FractureStage newStage) {
        super(player);
        this.newStage = newStage;

    }

    public FractureStage getNewStage() {
        return newStage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public void setNewStage(FractureStage newStage) {
        this.newStage = newStage;
    }
}
