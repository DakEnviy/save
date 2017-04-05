package ru.dakenviy.minez.event.player;

import org.bukkit.event.HandlerList;
import ru.dakenviy.minez.minez.Order;
import ru.dakenviy.minez.player.MineZPlayer;
import ru.dakenviy.minez.minez.MineZMiniGame;

public class PlayerOrderEvent extends MineZPlayerEvent {

    private Order order;
    private MineZMiniGame miniGame;

    public PlayerOrderEvent(MineZPlayer victim, MineZPlayer customer, int cost) {
        super(victim);
        miniGame = (MineZMiniGame) customer.getMiniGame();
        order = new Order(victim, customer, cost);
    }

    public PlayerOrderEvent(Order order, MineZMiniGame miniGame) {
        super(order.getVictim());
        this.order = order;
        this.miniGame = miniGame;
    }

    private final static HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Order getOrder() {
        return order;
    }

    public MineZMiniGame getMiniGame() {
        return miniGame;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
