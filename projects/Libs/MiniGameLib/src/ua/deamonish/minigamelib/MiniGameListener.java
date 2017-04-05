package ua.deamonish.minigamelib;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ua.deamonish.minigamelib.events.MiniGameJoinEvent;
import ua.deamonish.minigamelib.events.MiniGameQuitEvent;

public class MiniGameListener implements Listener {

    @EventHandler
    public void miniGameJoin(MiniGameJoinEvent event) {
        MiniGame miniGame = event.getMiniGame();
        Bukkit.getLogger()
                .info("Игрок " + event.getPlayer()
                        .getPlayer()
                        .getName() + " зашел в миниигру " + miniGame.getMiniGameName() + " [" + miniGame
                        .getOnline() + "/" + miniGame
                        .getMaxPlayerSize() + "]");
    }

    @EventHandler
    public void miniGameQuit(MiniGameQuitEvent event) {
        MiniGame miniGame = event.getMiniGame();
        Bukkit.getLogger()
                .info("Игрок " + event.getPlayer()
                        .getPlayer()
                        .getName() + " вышел с миниигры " + miniGame.getMiniGameName() + " [" + miniGame
                        .getOnline() + "/" + miniGame
                        .getMaxPlayerSize() + "]");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        MiniGamePlayer player = MiniGameManager.getInstance().getMiniGamePlayer(event.getEntity());
        if(player != null) {
            player.death(event);
        }
    }
}
