package ru.dakenviy.minez.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.minigamelib.MiniGamePlayer;
import ua.deamonish.modulesystem.modules.items.CustomItems;
import ua.deamonish.modulesystem.util.Config;

public class ModuleChat extends ua.deamonish.modulesystem.modules.chat.ModuleChat {

    public ModuleChat(Plugin plugin) {
        super(plugin);
    }

    private int delay = 2;
    private String delayMessage = "§cНельзя так часто писать в чат.";

    @Override
    public void onEnable() {
        super.onEnable();
        Config config = getConfig();
        config.setIfNotExist("delay", delay);
        config.setIfNotExist("delay-message", delayMessage);
        this.delay = config.getInt("delay");
        this.delayMessage = config.getStringColor(delayMessage);

    }

    @Override
    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        try {
            Player player = event.getPlayer();
            MiniGamePlayer miniGamePlayer = MiniGameManager.getInstance().getMiniGamePlayer(player);
            if (!player.hasPermission("deamonish.chat.delay")) {
                Long time = miniGamePlayer.getMetadata("last_chat");
                long current = System.currentTimeMillis();
                if (time == null || (current - time) / 1000 > this.delay) {
                    miniGamePlayer.setMetadata("last_chat", current);
                } else {
                    player.sendMessage(delayMessage);
                    event.setCancelled(true);
                    return;
                }
            }

            boolean global = false;
            if (player.getInventory().getItemInMainHand().equals(CustomItems.getItem("radio")) || player.getInventory()
                    .getItemInOffHand().equals(CustomItems.getItem("radio"))) {
                global = true;
            }
            sendChat(event, global);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
