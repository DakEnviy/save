package ua.deamonish.modulesystem.modules.chat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.module.ModuleManager;
import ua.deamonish.modulesystem.util.Config;
import ua.deamonish.modulesystem.util.StringUtil;

import java.util.Set;
import java.util.regex.Pattern;

public class ModuleChat extends Module implements Listener {

    private String formatChat = "&7[%type%]&7 %player%: &e%message%";
    private String local = "&aL";
    private String global = "&cG";
    private int distance = 100;
    private boolean enableGlobalLocal = false;


    private Pattern pattern;
    private String regular = ".*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|\\.(org|net|ru|en|com|pro)).*";

    public ModuleChat(Plugin plugin) {
        super("chat", ModuleManager.PRIORITY_NORMAL, plugin, new Config(plugin, "chat.yml"));
    }

    @Override
    public void onEnable() {
        this.registerListenersThis();
        Config config = this.getConfig();
        config.setIfNotExist("format", formatChat);
        config.setIfNotExist("local", local);
        config.setIfNotExist("global", global);
        config.setIfNotExist("local-distance", distance);
        config.setIfNotExist("regular", regular);
        config.setIfNotExist("enable-global-local", true);

        this.enableGlobalLocal = config.getBoolean("enable-global-local");
        this.formatChat = config.getString("format").replace("&", "§").replace("%player%", "%1$s")
                .replace("%message%", "%2$s");
        this.regular = config.getString("regular");
        this.local = config.getStringColor("local");
        this.distance = config.getInt("local-distance");
        this.global = config.getStringColor("global");
        this.pattern = Pattern.compile(this.regular);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onReload() {
        this.getConfig().reload();
        this.onEnable();
    }

    public String getFormatChat() {
        return formatChat;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        char[] value = message.toCharArray();

        if (value.length == 0) {
            event.setCancelled(true);
            return;
        }

        boolean global = false;
        if (value[0] == '!') {
            global = true;
        }
        sendChat(event, global);
    }

    protected void sendChat(AsyncPlayerChatEvent event, boolean global) {
        char[] value = event.getMessage().toCharArray();
        Player player = event.getPlayer();

        if (!this.enableGlobalLocal || !global) {
            Set<Player> players = event.getRecipients();
            players.clear();
            Location loc = player.getLocation();
            for (Player online : player.getWorld().getPlayers()) {
                if (online.getLocation().distance(loc) <= this.distance) {
                    players.add(online);
                }
            }
        }
        try {
            if (player.hasPermission("deamonish.chat.color")) {
                for (int i = 0; i < value.length; i++) {
                    if (value[i] == '&' && i != value.length - 1) {
                        if (value[i + 1] != '0' || value[1 + i] != '1') {
                            value[i] = '§';
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String message = new String(value);
        event.setMessage(message);

        try {
            String format = formatChat;
            format = StringUtil.replace(format, "%type%", global ? this.global : this.local, 1);
            event.setFormat(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pattern getPattern() {
        return pattern;
    }
}
