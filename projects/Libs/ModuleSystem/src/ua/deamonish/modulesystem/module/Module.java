package ua.deamonish.modulesystem.module;

import com.sun.istack.internal.NotNull;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ua.deamonish.modulesystem.module.event.ModuleDataType;
import ua.deamonish.modulesystem.util.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс представляющий модуль. Объекты этого класса, необходимо регестрировать модуль менеджером.
 */
public abstract class Module implements Listener {

	/**
	 * Получить объект модуля по указаному классу
	 * @param moduleClass класс модуля
	 * @return модуль, если такой имеется
 	 */
	@NotNull
	@SuppressWarnings("unchecked")
	public static <T> T  getInstance(Class<T> moduleClass) {
		if (moduleClass.equals(Module.class)) {
			throw new RuntimeException("нельзя указывать Module.class");
		}


		return (T) ModuleManager.getModules().stream()
				.filter(module -> module.getClass().equals(moduleClass))
				.findFirst().orElse(null);
	}

	private Plugin plugin;
	private String name; //Имя модуля
	private boolean enabled = false; //Включен ли
	private ModuleLogger logger; //logger
	private int priority; //приоритет

	private List<Object>        datas      = new ArrayList<>(); //дата плагина (эвенты и прочее)

	private Config config;

	public Module(String name, int priority, Plugin plugin, Config config) {
		this.name = name;
		this.plugin = plugin;
		this.logger = new ModuleLogger(plugin, this);
		this.config = config;
		this.priority = priority;
	}

	/**
	 * Запускается только при первой загрузке модуля<br>
	 * Идеально подходит для регистрации эвентов и т.п.
	 */
	public void onFirstEnable() {
		//.....
	}

	public abstract void onEnable();

	public abstract void onDisable();

	public void onReload() {
		this.standartReload();
	}

	/**
	 * Попытаться включить модуль
	 */
	public void enable() {
		if (!this.enabled) {
			this.onFirstEnable();
		}
		this.onEnable(); //Загружаем все дела
		this.enabled = true; //включен

	}

	protected void disable() {

		this.onDisable();

		this.unregisterDataAll();

		this.enabled = false;
		logger.info("Модуль '" + this.name + "' выключен.");
	}

	protected void reload() {
		this.onReload();
		logger.info("Модуль '" + this.name + "' перезагружен.");
	}

	public String getName() {
		return name;
	}

	public final boolean isEnable() {
		return enabled;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public Config getConfig() {
		return config;
	}

	public ModuleLogger getLogger() {
		return logger;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * Стандартный варинт перезагрузки модуля, в именно:
	 *  1 перезагрузка конфига
	 *  2 выключение onDisable
	 *  3 включение onEnable
	 */
	protected void standartReload() {
		if (config != null) {
			this.config.reload();
		}
		this.onDisable();
		this.onEnable();
	}

	/**
	 * Зарегистрировать дату модуля (например, bukkit эвенты)<br>
	 * Смотреть или добавить возможную дату модуля можно тут - <code>ModuleDataType</code>
	 * @param o дата
	 */
	public void registerData(Object o) {
		for (Object data : this.datas) {
			// Отменяем попытку зарагестрировать эвенты дважды.
			if (data.getClass().equals(o.getClass())) {
				return;
			}
		}
		ModuleDataType type = ModuleDataType.getTypeByClass(o);
		if (type == null) {
			throw new ModuleException("Плагин не поддерживает такую дату: " + o.toString());
		}
		type.getControler().register(this, o);
		this.datas.add(o);
	}

	/**
	 * Удалить дату модуля
	 * @param o дата
	 */
	public void unregisterData(Object o) {
		ModuleDataType type = ModuleDataType.getTypeByClass(o);
		if (type == null) {
			throw new ModuleException("Плагин не поддерживает такую дату: " + o.toString());
		}
		type.getControler().unregister(this, o);
		this.datas.remove(o);
	}

	/**
	 * Удалить всю дату
	 */
	public void unregisterDataAll() {
		for (Object data : this.datas) {
			ModuleDataType type = ModuleDataType.getTypeByClass(data);
			if (type != null) {
				type.getControler().unregister(this, data);
			}
		}
		this.datas.clear();
	}

	/**
	 * Зарегестрировать текущий класс, как эвенты
	 * Если использовать много раз, сработает только первый
	 */
	public void registerListenersThis() {
		this.registerData(this);
	}


	/**
	 * Загрегистрировать эвенты
	 * @param listener - объект, который регестрируем.
	 * @deprecated используйте <code>registerData</code>
	 */
	@Deprecated
	protected void registerListeners(Listener listener) {
		this.registerData(listener);
	}
}
