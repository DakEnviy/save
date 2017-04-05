package ua.deamonish.modulesystem.module;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class ModuleListener implements Listener {


	/**
	 * Выключение сервера
	 */
	@EventHandler
	public void onPluginDisableEvent(PluginDisableEvent event) {
		// если выключается плагин, который содержит в себе хоть один модуль, то вырубаем все модули
		if (ModuleManager.isEnableServer() && ModuleManager.getModules().stream()
				.filter(module -> module.getPlugin().equals(event.getPlugin()))
				.findFirst().isPresent()) {
			ModuleManager.disableModules();
		}
	}
}
