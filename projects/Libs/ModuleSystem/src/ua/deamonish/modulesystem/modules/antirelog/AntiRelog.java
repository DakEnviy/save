package ua.deamonish.modulesystem.modules.antirelog;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public interface AntiRelog {

    void entityCombat(EntityDamageByEntityEvent event);

    void playerQuit(PlayerQuitEvent event);
}
