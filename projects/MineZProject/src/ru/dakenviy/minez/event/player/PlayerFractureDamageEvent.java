package ru.dakenviy.minez.event.player;

import org.bukkit.event.HandlerList;
import ru.dakenviy.minez.player.MineZPlayer;

public class PlayerFractureDamageEvent extends MineZPlayerEvent {

    private final static HandlerList handlers = new HandlerList();
    private double damage;

    public PlayerFractureDamageEvent(MineZPlayer player, double damage) {
        super(player);
        this.damage = damage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }
}
