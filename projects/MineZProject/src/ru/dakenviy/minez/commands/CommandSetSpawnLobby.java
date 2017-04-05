package ru.dakenviy.minez.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ua.deamonish.modulesystem.modules.command.Command;

import java.util.List;

/**
 * Команда устанавливает позицию спавна, для лобби.
 */
public class CommandSetSpawnLobby extends Command {

    private ModuleMineZ moduleMineZ = ModuleMineZ.getInstance(ModuleMineZ.class);

    public CommandSetSpawnLobby() {
        super("setspawnlobby", "deamonish.command.admin");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.checkIsConsole();
        Player player = (Player) sender;
        Location loc = player.getLocation();
        if (!loc.getWorld().getName().equals(moduleMineZ.getLobbyWorld().getName())) {
            player.sendMessage("§cВаш мир отличается от мира лобби, указаного в конфиге.");
            return false;
        }
        moduleMineZ.setLobbySpawnPoint(player.getLocation());
        player.sendMessage("§aВы установили позицию спавна.");
        return false;
    }
}
