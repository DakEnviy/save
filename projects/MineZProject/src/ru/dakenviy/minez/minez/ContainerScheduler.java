package ru.dakenviy.minez.minez;

import ru.dakenviy.minez.containers.MineZArmorStand;
import ru.dakenviy.minez.containers.MineZChest;
import ua.deamonish.minigamelib.MiniGame;
import ua.deamonish.minigamelib.MiniGameManager;

/**
 * Задача, что выполняется в отдельном потоке. Генерирует новый лут, во всех сундуках и на арморстендах.
 */
public class ContainerScheduler implements Runnable {

    private MiniGameManager manager = MiniGameManager.getInstance();

    @Override
    public void run() {
        for(MiniGame game : manager.getMiniGames().values()){
            MineZMiniGame mineZMiniGame = (MineZMiniGame) game;
            mineZMiniGame.getChests().forEach(MineZChest::generateLoot);
            mineZMiniGame.getStands().forEach(MineZArmorStand::playRandom);
        }
    }
}
