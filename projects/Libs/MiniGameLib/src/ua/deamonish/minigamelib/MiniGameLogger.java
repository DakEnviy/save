package ua.deamonish.minigamelib;

import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MiniGameLogger extends Logger {

    private String prefix;

    public MiniGameLogger(Plugin context, MiniGame miniGame) {
        super(context.getClass().getCanonicalName(), null);
        String prefix = context.getDescription().getPrefix();
        this.prefix = prefix != null ? "[" + prefix + "] " : "[" + context.getDescription().getName() + "] ";
        this.prefix += "[" + miniGame.getMiniGameName() + "] ";
        this.setParent(context.getLogger());
        this.setLevel(Level.ALL);
    }

    public void log(LogRecord logRecord) {
        logRecord.setMessage(this.prefix + logRecord.getMessage());
        super.log(logRecord);
    }
}