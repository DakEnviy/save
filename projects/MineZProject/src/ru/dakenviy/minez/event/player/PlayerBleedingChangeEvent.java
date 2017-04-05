package ru.dakenviy.minez.event.player;

import org.bukkit.event.HandlerList;
import ru.dakenviy.minez.player.MineZPlayer;
import ru.dakenviy.minez.stages.BleedingStage;

public class PlayerBleedingChangeEvent extends MineZPlayerEvent {

    private final static HandlerList handlers = new HandlerList();
    private BleedingStage newStage;

    public PlayerBleedingChangeEvent(MineZPlayer player, BleedingStage newStage) {
        super(player);
        this.newStage = newStage;
    }

    public BleedingStage getNewStage() {
        return newStage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public void setNewStage(BleedingStage newStage) {
        this.newStage = newStage;
    }

}
