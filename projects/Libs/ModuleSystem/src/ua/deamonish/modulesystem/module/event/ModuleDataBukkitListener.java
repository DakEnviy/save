package ua.deamonish.modulesystem.module.event;

import ua.deamonish.modulesystem.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
;


/**
 * Регистрация и удаление эвентов
 */
public class ModuleDataBukkitListener implements ModuleData {

	@Override
	public void register(Module module, Object o) {
		Listener listener = (Listener) o;
		Bukkit.getPluginManager().registerEvents(listener, module.getPlugin());
	}

	@Override
	public void unregister(Module module, Object o) {
		Listener listener = (Listener) o;
		HandlerList.unregisterAll(listener);
	}
}
