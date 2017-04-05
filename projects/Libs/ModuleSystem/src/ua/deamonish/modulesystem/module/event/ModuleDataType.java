package ua.deamonish.modulesystem.module.event;

import com.comphenix.protocol.events.PacketAdapter;
import org.bukkit.event.Listener;

/**
 * Типы созможной сохраняемой даты
 */
public enum ModuleDataType {
	BUKKIT_LISTENER(Listener.class, new ModuleDataBukkitListener()),
	PROTOCOL_LIB_LISTENER(PacketAdapter.class, new ModuleDataProtocolLibListener()),


	;

	private Class type;
	private ModuleData control;


	ModuleDataType(Class type, ModuleData control) {
		this.type = type;
		this.control = control;
	}

	/**
	 * Получить обработчик даты
	 * @return обработчик даты
	 */
	public ModuleData getControler() {
		return control;
	}

	/**
	 * Получить тип даты по имени
	 * @param o дата
	 * @return тип даты
	 */
	public static ModuleDataType getTypeByClass(Object o) {
		for (ModuleDataType type : values()) {
			if (type.type.isInstance(o)) {
				return type;
			}
		}
		return null;
	}
}
