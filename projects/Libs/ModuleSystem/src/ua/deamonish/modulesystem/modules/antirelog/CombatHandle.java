package ua.deamonish.modulesystem.modules.antirelog;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import ua.deamonish.modulesystem.modules.messages.Messages;

public class CombatHandle {

    private BossBar busyBar;
    private BossBar freeBar;
    private Player player;
    private boolean inCombat;
    private int combatTickTask;
    private ModuleAntiRelog relog = ModuleAntiRelog.getInstance(ModuleAntiRelog.class);
    private int combatTimeLeft;
    private String chatMessageBusy;
    private String chatMessageFree;

    public CombatHandle(final Player player) {
        combatTickTask = -1;
        chatMessageBusy = Messages.getMessage("busy_chat", "%timeleft%", String.valueOf(relog.getCombatTimeOut()), "%player%", player.getDisplayName());
        chatMessageFree = Messages.getMessage("free_chat", "%timeleft%", String.valueOf(relog.getCombatTimeOut()), "%player%", player.getDisplayName());
        this.player = player;
        if (relog.isBarEnable()) {
            (busyBar = Bukkit.createBossBar(Messages.getMessage("busy_bar").replace("%timeleft%", String.valueOf(combatTimeLeft)), relog.getBusyColor(), relog.getBarStyle())).addPlayer(player);
            busyBar.setVisible(false);
            (freeBar = Bukkit.createBossBar(Messages.getMessage("free_bar"), relog.getFreeColor(), relog.getBarStyle())).addPlayer(player);
            freeBar.setVisible(false);
        }
        inCombat = false;
    }

    public boolean shouldBePunished() {
        /*SL:72*/
        return inCombat;
    }

    public void startCombat() {
        if (relog.isBarEnable()) {
            busyBar.setTitle(Messages.getMessage("busy_bar", "%timeleft%", String.valueOf(combatTimeLeft)));
            busyBar.setVisible(true);
            busyBar.setProgress(1.0);
            freeBar.setVisible(false);
        }

        if (!chatMessageBusy.isEmpty() && !inCombat) {
            player.sendMessage(chatMessageBusy);
        }

        combatTimeLeft = relog.getCombatTimeOut();
        if (combatTickTask != -1) {
            Bukkit.getScheduler().cancelTask(combatTickTask);
        }

        combatTickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(relog.getPlugin(), () -> {
            if (combatTimeLeft == 0) {
                this.endCombat();
            } else {
                player.playNote(player.getLocation(), Instrument.PIANO, Note.sharp(1, Note.Tone.C));
                if (relog.isBarEnable()) {
                    busyBar.setProgress((double) combatTimeLeft / (double) relog.getCombatTimeOut());
                    busyBar.setTitle(Messages.getMessage("busy_bar", "%timeleft%", String.valueOf(combatTimeLeft)));
                }
                --combatTimeLeft;
            }
        }, 0L, 20L);

        inCombat = true;
    }

    public void endCombat() {
        if (relog.isBarEnable()) {
            busyBar.setVisible(false);
            freeBar.setVisible(true);
            freeBar.setProgress(1.0);
            Bukkit.getScheduler().scheduleSyncDelayedTask(relog.getPlugin(), () -> freeBar.setVisible(false), (long) (relog.getVanishTimeOut() * 20));
        }
        Bukkit.getServer().getScheduler().cancelTask(combatTickTask);
        combatTickTask = -1;
        player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.G));
        if (!chatMessageFree.isEmpty()) {
            player.sendMessage(chatMessageFree);
        }
        inCombat = false;
    }

    void cleanUp() {
        if (relog.isBarEnable()) {
            busyBar.removeAll();
            freeBar.removeAll();
        }
    }
}
