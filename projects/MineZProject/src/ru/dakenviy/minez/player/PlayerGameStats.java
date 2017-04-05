package ru.dakenviy.minez.player;

/**
 * Класс в котором хранится статистика игрока, за период его игровой сессии. (сколько убил, вылечил и т.д)
 */
public class PlayerGameStats {

    private int killingPlayers;
    private int killingZombies;
    private int healingPlayers;
    private int killingGiants;
    public int getKillingPlayers() {
        return killingPlayers;
    }

    public int getKillingZombies() {
        return killingZombies;
    }

    public int getHealingPlayers() {
        return healingPlayers;
    }

    public void incriminateKillingPlayer() {
        this.killingPlayers++;
    }

    public void incriminateKillingZombie() {
        this.killingZombies++;
    }

    public void incriminateHealingPlayers() {
        this.healingPlayers++;
    }

    void saveStatistic(){

    }
}
