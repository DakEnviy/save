package ua.deamonish.modulesystem.modules.antirelog;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CopyOnWriteArrayList;

public class AntiRelogComposite implements AntiRelog, Listener{

    private static AntiRelogComposite instance;

    private AntiRelogComposite() {
    }

    private CopyOnWriteArrayList<AntiRelog> handlers = new CopyOnWriteArrayList<>();

    public static AntiRelogComposite getInstance() {
        if (instance == null) {
            instance = new AntiRelogComposite();
        }
        return instance;
    }

    public void register(AntiRelog relog) {
        handlers.add(relog);
    }

    public void unregister(AntiRelog relog) {
        handlers.remove(relog);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Override
    public void entityCombat(EntityDamageByEntityEvent event) {
        for (AntiRelog relog : handlers) {
            relog.entityCombat(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Override
    public void playerQuit(PlayerQuitEvent event) {
        for (AntiRelog relog : handlers) {
            relog.playerQuit(event);
        }
        event.getPlayer().setHealth(0.0D);
    }
}
