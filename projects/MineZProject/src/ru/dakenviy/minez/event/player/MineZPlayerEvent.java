package ru.dakenviy.minez.event.player;

import org.bukkit.event.Event;
import ru.dakenviy.minez.player.MineZPlayer;

public abstract class MineZPlayerEvent extends Event {

    private MineZPlayer player;

    public MineZPlayerEvent(MineZPlayer player) {
        super(false);
        this.player = player;
    }

    public MineZPlayer getPlayer() {
        return player;
    }

    public void setPlayer(MineZPlayer player) {
        this.player = player;
    }

}
