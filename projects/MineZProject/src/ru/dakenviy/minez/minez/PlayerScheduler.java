package ru.dakenviy.minez.minez;

import org.bukkit.Bukkit;
import ru.dakenviy.minez.event.fullmoon.FullMoonChangeEvent;
import ru.dakenviy.minez.event.player.*;
import ru.dakenviy.minez.player.MineZPlayer;
import ru.dakenviy.minez.stages.BleedingStage;
import ru.dakenviy.minez.stages.FractureStage;
import ua.deamonish.minigamelib.MiniGame;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.minigamelib.MiniGamePlayer;
import ua.deamonish.modulesystem.module.Module;

/**
 * Класс-задача, что отслеживает состояния игроков(переломы, жажду, кровопотерю).
 * Сделан в связи с недоверием к Bukkit.getScheduler(), ведь там период указывается в тиках.
 * А TPS сервера зависит от нагруженности.
 */
public class PlayerScheduler implements Runnable {

    private MiniGameManager manager = MiniGameManager.getInstance();
    private ModuleMineZ mineZ = Module.getInstance(ModuleMineZ.class);
    private int dayCount = 0;
    private int timeFullMoon = 0;

    @Override
    public void run() {
        for (MiniGamePlayer miniGamePlayer : manager.getMiniGamePlayers()) {
            MineZPlayer mineZPlayer = (MineZPlayer) miniGamePlayer;
            if (mineZPlayer.isPlaying()) {
                if (mineZPlayer.isBleeding()) {
                    long lastBleeding = mineZPlayer.getBleedingTimer();
                    mineZPlayer.setBleedingTimer(++lastBleeding);
                    BleedingStage current = mineZPlayer.getCurrentBleedingStage();
                    BleedingStage nextStage;
                    if (current == null) {
                        nextStage = BleedingStage.LOW;
                    } else {
                        nextStage = current.getNextStage();
                    }
                    if (current != null && lastBleeding % ModuleMineZ.getBleedingPeriod() == 0) {
                        Bukkit.getPluginManager().callEvent(new PlayerBleedingDamageEvent(mineZPlayer, mineZPlayer.getCurrentBleedingStage().getDamage()));
                    }
                    if (lastBleeding >= nextStage.getTime() && nextStage != mineZPlayer.getCurrentBleedingStage()) {
                        Bukkit.getPluginManager().callEvent(new PlayerBleedingChangeEvent(mineZPlayer, nextStage));
                    }
                }
                if (mineZPlayer.isFracture()) {
                    mineZPlayer.setFractureTimer(mineZPlayer.getFractureTimer() + 1);
                    FractureStage currentStage =  mineZPlayer.getCurrentFractureStage();
                    FractureStage nextStage;
                    if(currentStage != null){
                        nextStage = currentStage.getNextStage();
                    } else {
                        nextStage = FractureStage.LOW;
                    }
                    if (mineZPlayer.getFractureTimer() >= nextStage.getTime() && nextStage != mineZPlayer.getCurrentFractureStage()) {
                        Bukkit.getPluginManager().callEvent(new PlayerFractureChangeEvent(mineZPlayer, nextStage));
                    }
                }
                long lastChangeThirst = mineZPlayer.getLastChangeThirsts();
                mineZPlayer.setLastChangeThirsts(++lastChangeThirst);
                if (lastChangeThirst % ModuleMineZ.getChangeThirstTime() == 0) {
                    Bukkit.getPluginManager().callEvent(new PlayerThirstChangeEvent(mineZPlayer, 1, false));
                }
                if(mineZPlayer.getThirsts() == 0) {
                    if(mineZPlayer.getDamageThirstTimer() % ModuleMineZ.getThirstDamagePeriod() == 0){
                        Bukkit.getPluginManager().callEvent(new PlayerThirstDamageEvent(mineZPlayer, ModuleMineZ.getThirstDamage()));
                    }
                    mineZPlayer.setDamageThirstTimer(mineZPlayer.getDamageThirstTimer()+1);
                } else {
                    if(mineZPlayer.getDamageThirstTimer() != 0) {
                        mineZPlayer.setDamageThirstTimer(0);
                    }
                }
            }
        }
        if (mineZ.getLobbyWorld().getTime() > 18000 && 18021 >= mineZ.getLobbyWorld().getTime()){
            dayCount++;
            if(dayCount == ModuleMineZ.getFullMoonPeriod()){
                dayCount = 0;
                Bukkit.getPluginManager().callEvent(new FullMoonChangeEvent(true));
            }
        }
        if(ModuleMineZ.isFullMoon()){
            for(MiniGame game : manager.getMiniGame()){
                game.getWorld().setTime(18000);
            }
            timeFullMoon++;
            if(timeFullMoon == ModuleMineZ.getFullMoonTime()){
                timeFullMoon = 0;
                Bukkit.getPluginManager().callEvent(new FullMoonChangeEvent(false));
            }
        }
    }
}
