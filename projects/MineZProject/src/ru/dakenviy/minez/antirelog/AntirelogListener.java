package ru.dakenviy.minez.antirelog;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import ru.dakenviy.minez.player.MineZPlayer;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.modules.antirelog.AntiRelog;
import ua.deamonish.modulesystem.modules.antirelog.AntiRelogComposite;
import ua.deamonish.modulesystem.modules.antirelog.CombatHandle;
import ua.deamonish.modulesystem.modules.antirelog.ModuleAntiRelog;
import ua.deamonish.modulesystem.modules.items.CustomItems;
import ua.deamonish.modulesystem.modules.messages.Messages;

public class AntirelogListener implements AntiRelog {

    private ModuleAntiRelog relog = Module.getInstance(ModuleAntiRelog.class);
    private MiniGameManager manager = MiniGameManager.getInstance();

    public AntirelogListener() {
        AntiRelogComposite.getInstance().register(this);
    }

    @Override
    public void entityCombat(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && relog.isSubject(event.getEntity().getType())) {
            Player damager = (Player) event.getDamager();
            MineZPlayer damagerPlayer = manager.getMiniGamePlayer(damager);
            if (event.getEntityType() == EntityType.PLAYER) {
                ItemStack mainItem = damager.getInventory().getItemInMainHand();
                if (mainItem != null && mainItem.equals(CustomItems.getItem("bandage"))) { // Бинт не вешает комбат.
                    event.setDamage(0.01);
                    MineZPlayer player = manager.getMiniGamePlayer((Player) event.getEntity());
                    player.setMetadata("bandage", damager);
                    return;
                }
                damagerPlayer.getCombatHandle().startCombat();
            }
        }
        if (event.getEntity().getType() == EntityType.PLAYER) {
            MineZPlayer player = manager.getMiniGamePlayer((Player) event.getEntity());
            player.getCombatHandle().startCombat();
        }

        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            MineZPlayer player = manager.getMiniGamePlayer((Player) ((Projectile) event.getDamager()).getShooter());
            player.getCombatHandle().startCombat();
        }
    }

    @Override
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MineZPlayer p = manager.getMiniGamePlayer(player);
        CombatHandle handle = p.getCombatHandle();
        if (handle.shouldBePunished() && !player.hasPermission("deamonish.antirelog")) {
            String message = Messages.getMessage("broadcast_message", "%player%", player.getDisplayName());
            if (!message.isEmpty()) {
                p.getMiniGame().broadcastMessage(message);
            }
            player.setHealth(0.0D);
        }
    }

}
