package ua.deamonish.modulesystem.command;

import ua.deamonish.modulesystem.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ua.deamonish.modulesystem.module.ModuleManager;
import ua.deamonish.modulesystem.util.MyObject;


import java.util.ArrayList;
import java.util.Map;

/**
 * Модуль команд
 */
public class ModuleCommand extends Module {

	private static ModuleCommand instance;
	private final String metadataHistory = "command_history";

	public ModuleCommand (Plugin plugin) {
		super("command", ModuleManager.PRIORITY_LOW, plugin, null);
		instance = this;
	}

	public static ModuleCommand getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public final Map<String, org.bukkit.command.Command> knownCommands =
			new MyObject(Bukkit.getServer())
					.getField("commandMap")
					.getField("knownCommands")
					.getObject();

	private ArrayList<org.bukkit.command.Command> removed = new ArrayList<>();

	// Связка команд и их объектов
	private ArrayList<org.bukkit.command.Command> commands = new ArrayList<>();




	/**
	 * Зарегистировать команду
	 * @param command - команда, которую нужно зарегистрировать.
	 */
	public void registerCommand(org.bukkit.command.Command command) {
		knownCommands.remove(command.getName());
		knownCommands.put(command.getName(), command);
		for (String a : command.getAliases()) {
			knownCommands.remove(a);
			knownCommands.put(a, command);
		}
		commands.add(command);
	}


	/**
	 * Получить все команды
	 * @return команды
	 */
	public ArrayList<org.bukkit.command.Command> getCommands() {
		return commands;
	}

	/**
	 * Получить команду по названию
	 * @param command название команды
	 * @return команду
	 */
	public org.bukkit.command.Command getCommand(String command) {
		return knownCommands.get(command);
	}


	/**
	 * Удалить команду
	 * @param command - команда.
	 */
	public void unregisterCommand(String command) {
		org.bukkit.command.Command c = knownCommands.remove(command);
		if (c != null) {
			knownCommands.remove(c.getName());
			for (String a : c.getAliases())
				knownCommands.remove(a);
		}
		commands.remove(c);
	}


	@Override
	public void onEnable () {

	}

	@Override
	public void onDisable () {

	}
}
