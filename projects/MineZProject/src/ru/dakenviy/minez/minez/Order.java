package ru.dakenviy.minez.minez;

import ru.dakenviy.minez.player.MineZPlayer;

/**
 * Класс отвечающий за заказ. Содержит customer - заказчика, victim - жертву, cost - ставку.
 */
public class Order {

    private MineZPlayer victim;
    private MineZPlayer customer;
    private int cost;

    public Order(MineZPlayer victim, MineZPlayer customer, int cost) {
        this.victim = victim;
        this.customer = customer;
        this.cost = cost;
    }

    public MineZPlayer getVictim() {
        return victim;
    }

    public MineZPlayer getCustomer() {
        return customer;
    }

    public int getCost() {
        return cost;
    }

    public void setVictim(MineZPlayer victim) {
        this.victim = victim;
    }

    public void setCustomer(MineZPlayer customer) {
        this.customer = customer;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
