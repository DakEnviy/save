package ua.deamonish.modulesystem.module;

import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {

	private String pluginName;

	public ModuleLogger(Plugin context, Module module) {
		super(context.getClass().getCanonicalName(), null);
		String prefix = context.getDescription().getPrefix();
		this.pluginName = prefix != null?"[" + prefix + "] ":"[" + context.getDescription().getName() + "] ";
		this.pluginName += "[" +  module.getName() + "] ";
		this.setParent(context.getLogger());
		this.setLevel(Level.ALL);
	}

	public void log(LogRecord logRecord) {
		logRecord.setMessage(this.pluginName + logRecord.getMessage());
		super.log(logRecord);
	}

	public void severeLoadParameter(String data) {
		this.severe("Ошибка загрузки значения '" + data + "'.");
	}
}
