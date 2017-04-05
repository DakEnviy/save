package ua.deamonish.modulesystem.command;

import org.bukkit.command.Command;

import java.util.ArrayList;
import java.util.Map;

/**
 * Менеджер, через который необходимо регестрировать команды.
 */
public class CommandManager {

	private static ModuleCommand moduleCommand = ModuleCommand.getInstance();

	public static ModuleCommand getModuleCommand() {
		return moduleCommand;
	}

	public static final Map<String, Command> knownCommands = moduleCommand.knownCommands;

	/**
	 * Зарегистировать команду
	 * @param command - команда, которую нужно зарегистрировать.
	 */
	public static void registerCommand(Command command) {
		moduleCommand.registerCommand(command);
	}


	/**
	 * Получить все команды
	 * @return команды
	 */
	public static ArrayList<Command> getCommands() {
		return moduleCommand.getCommands();
	}

	/**
	 * Получить команду по названию
	 * @param command название команды
	 * @return команду
	 */
	public static Command getCommand(String command) {
		return moduleCommand.getCommand(command);
	}


	/**
	 * Удалить команду
	 * @param command команда.
	 */
	public static void unregisterCommand(String command) {
		moduleCommand.unregisterCommand(command);
	}
}