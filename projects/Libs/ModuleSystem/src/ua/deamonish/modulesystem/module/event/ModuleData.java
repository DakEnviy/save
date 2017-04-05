package ua.deamonish.modulesystem.module.event;


import ua.deamonish.modulesystem.module.Module;

/**
 * Дата модуля, которая связана должна контролироваться при запуске или выключении модуля
 */
public interface ModuleData {

	/**
	 * Регистрация даты
	 * @param module модуль
	 * @param o дата
	 */
	void register(Module module, Object o);

	/**
	 * Удаление даты
	 * @param module модуль
	 * @param o дата
	 */
	void unregister(Module module, Object o);
}
