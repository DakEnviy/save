package ua.deamonish.modulesystem.module;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

@SuppressWarnings("javadoc")
public class ModuleManager {

	//Priority
	public static final int PRIORITY_LOW    = -10;
	public static final int PRIORITY_NORMAL = 0;
	public static final int PRIORITY_HIGH   = 10;



	private static ArrayList<Module> modules = new ArrayList<>();

	private static boolean enableServer = false;

	public static boolean isEnableServer() {
		return enableServer;
	}

	public static void registerModule(Module module) throws ModuleException {
		if(getModule(module.getName()) != null) {
			throw new ModuleException("Модуль с именем '" + module.getName() + "' уже зарегистрирован!");
		}
		int i = 0;
		boolean added = false;
		for(Module m : modules) {
			if(module.getPriority() < m.getPriority()) {
				modules.add(i, module);
				added = true;
				break;
			}
			i++;
		}
		if(!added) {
			modules.add(module);
		}
	}

	/**
	 * Получить список модулей
	 *
	 * @return
	 */
	public static ArrayList<Module> getModules() {
		return modules;
	}

	/**
	 * Получить модуль по имени
	 *
	 * @param name
	 * @return
	 * @deprecated Устарело. use <code>Module::getInstance(Class<? extends Module>)</code>
	 */
	@Deprecated
	public static Module getModule(String name) {
		Module module = null;

		for(Module m : modules) {
			if(m.getName().equals(name)) {
				return m;
			} else if(m.getName().equalsIgnoreCase(name)) {
				module = m;
			}
		}

		return module;
	}

	public static void enableModules(Plugin plugin) {
		Bukkit.getScheduler().runTask(plugin, ()-> {
			for(Module module : modules) {
				try {
					if(!module.isEnable()) {
						module.enable();
					}
				} catch (Exception e) {
					Bukkit.getLogger().severe("При загрузке модуля '" + module.getName() + "' произошла ошибка. Выключаем сервер...");
					e.printStackTrace();
					try {
						disableModules();
					} catch(Exception eDisable) {
						eDisable.printStackTrace();
					}
					Bukkit.shutdown();
					return;
				}
			}
			enableServer = true;
		});
	}

	public static void disableModules() {
		enableServer = false;
		for(int i = modules.size() - 1; i > 0; i--) {
			Module module = modules.get(i);
			try {
				if(module.isEnable()) {
					module.disable();
				}
			} catch(Exception e) {
				Bukkit.getLogger().severe("При выключении модуля '" + module.getName() + "' произошла ошибка.");
				e.printStackTrace();
			}
		}
	}
}