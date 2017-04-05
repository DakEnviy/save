package ru.dakenviy.minez.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.modules.command.Command;

import java.util.List;

/**
 * Команда, которой устанавливаем позиции для спавна. Ставить их можно в любом мире(Во всех мирах, они будут одинаковы).
 */
public class CommandSetSpawnPoint extends Command {

    private ModuleMineZ mineZ = Module.getInstance(ModuleMineZ.class);

    public CommandSetSpawnPoint() {
        super("setspawnpoint", "deamonish.command.admin");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.checkIsConsole();
        Player player = (Player) sender;
        mineZ.getDefaultSpawnPoint().add(player.getLocation());
        mineZ.recalculateSpawnPosition();
        mineZ.saveSpawnPoints();
        player.sendMessage("§aВы успешно добавили спавн поинт.");
        return false;
    }
}
