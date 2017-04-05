package ua.deamonish.modulesystem.modules.antirelog;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.modules.messages.Messages;
import ua.deamonish.modulesystem.util.Config;

import java.util.ArrayList;
import java.util.List;

public class ModuleAntiRelog extends Module {

    public ModuleAntiRelog(Plugin plugin) {
        super("antirelog", 0, plugin, new Config(plugin, "antirelog.yml"));
    }

    private List<String> subjects = new ArrayList<>();
    private boolean barEnable;
    private boolean broadcastMessages;
    private BarColor busyColor;
    private BarColor freeColor;
    private BarStyle barStyle;
    private int combatTimeOut;
    private int vanishTimeOut;
    private Config config;


    @Override
    public void onEnable() {
        this.registerData(AntiRelogComposite.getInstance());
        config = getConfig();
        setDefaults();
        loadConfigValues();
    }

    private void setDefaults() {
        config.setIfNotExist("module_enabled", true);
        config.setIfNotExist("enable_bar", true);
        config.setIfNotExist("combat_time", 15);
        config.setIfNotExist("vanish_timeout", 5);
        config.setIfNotExist("busy_message", "&cНе выходите в течении&7: &r%timeleft% секунд.");
        config.setIfNotExist("free_message", "&aТеперь вы можете выйти.");
        config.setIfNotExist("busy_color", "red");
        config.setIfNotExist("free_color", "green");
        config.setIfNotExist("bar_style", "segmented_6");
        config.setIfNotExist("broadcast_message", "&b[MineZ] &6Игрок &2%player% &6вышел во время боя!");
        config.setIfNotExist("broadcast_message_send", true);
        config.setIfNotExist("busy_chat", "&c[MineZ] &fТеперь вы в &6бою&f! Бой закончится через %timeleft% секунд.");
        config.setIfNotExist("free_chat", "&a[MineZ] &fВы вышли из &6боя!");
        config.setIfNotExist("subjects", new String[]{"Player", "Zombie", "Husk", "Zombie_Villager"});
    }


    private void loadConfigValues() {
        try {
            if (!config.getBoolean("module_enabled")) {
                this.disable();
            }
            busyColor = BarColor.valueOf(config.getString("busy_color").toUpperCase());
            freeColor = BarColor.valueOf(config.getString("free_color").toUpperCase());
            barStyle = BarStyle.valueOf(config.getString("bar_style").toUpperCase());
            Messages.addMessage("busy_bar", config.getString("busy_message"));
            Messages.addMessage("free_bar", config.getString("free_message"));
            Messages.addMessage("broadcast_message", config.getString("broadcast_message"));
            Messages.addMessage("busy_chat", config.getString("busy_chat"));
            Messages.addMessage("free_chat", config.getString("free_chat"));
            subjects = config.getStringList("subjects");
            combatTimeOut = config.getInt("combat_time");
            vanishTimeOut = config.getInt("vanish_timeout");
            barEnable = config.getBoolean("enable_bar");
            broadcastMessages = config.getBoolean("broadcast_message_send");
        } catch (Exception ex){
            Bukkit.getLogger().severe("Ошибка при выгрузке значений из конфига, для модуля AntiRelog.");
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {

    }

    public boolean isSubject(final EntityType entity) {
        for (final String s : subjects) {
            if (s.toUpperCase().equals(entity.name())) {
                return true;
            }
        }
        return false;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public boolean isBarEnable() {
        return barEnable;
    }

    public boolean isBroadcastMessages() {
        return broadcastMessages;
    }

    public BarColor getBusyColor() {
        return busyColor;
    }

    public BarColor getFreeColor() {
        return freeColor;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public int getCombatTimeOut() {
        return combatTimeOut;
    }

    public int getVanishTimeOut() {
        return vanishTimeOut;
    }

}
