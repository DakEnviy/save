package ru.dakenviy.minez;

import org.bukkit.plugin.java.JavaPlugin;
import ru.dakenviy.minez.chat.ModuleChat;
import ru.dakenviy.minez.commands.*;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ua.deamonish.modulesystem.modules.antirelog.ModuleAntiRelog;
import ua.deamonish.modulesystem.modules.command.CommandManager;
import ua.deamonish.modulesystem.modules.command.ModuleCommand;
import ua.deamonish.modulesystem.modules.delay.ModuleDelay;
import ua.deamonish.modulesystem.module.ModuleManager;
import ua.deamonish.modulesystem.modules.saveworld.ModuleSaveWorlds;
import ua.deamonish.modulesystem.modules.mysql.ModuleMySQL;

/**
 * Основной класс MineZ.
 * В нем регестрируются все модули этого режима, и команды.
 */
public class MineZMain extends JavaPlugin {

    private static MineZMain instance;

    @Override
    public void onEnable() {
        instance = this;

        // Регестрируем Модули
        ModuleManager.registerModule(new ModuleDelay(this));
        ModuleManager.registerModule(new ModuleMySQL(this));
        ModuleManager.registerModule(new ModuleCommand(this));
        ModuleManager.registerModule(new ModuleAntiRelog(this));
        ModuleManager.registerModule(new ModuleMineZ(this));
        ModuleManager.registerModule(new ModuleSaveWorlds(this));
        ModuleManager.registerModule(new ModuleChat(this));

        // Регестрируем Команды
        CommandManager.registerCommand(new CommandTest());
        CommandManager.registerCommand(new CommandSetMiniGameTable());
        CommandManager.registerCommand(new CommandSetSpawnLobby());
        CommandManager.registerCommand(new CommandSetSpawnPoint());
        CommandManager.registerCommand(new CommandSpawn());
        CommandManager.registerCommand(new CommandSetChest());
        CommandManager.registerCommand(new CommandSetArmorStand());
        CommandManager.registerCommand(new Kill());
        CommandManager.registerCommand(new CommandStartFullMoon());
        CommandManager.registerCommand(new CommandAddMoney());
        CommandManager.registerCommand(new Dudos());

        // Включаем все модули.
        ModuleManager.enableModules(this);
    }

    @Override
    public void onDisable() {
        ModuleManager.disableModules();
    }

    public static MineZMain getInstance() {
        return instance;
    }
}
