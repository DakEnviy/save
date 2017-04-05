package ru.dakenviy.minez.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ru.dakenviy.minez.player.MineZPlayer;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.modulesystem.modules.command.Command;

import java.util.List;

public class CommandSpawn extends Command {
    public CommandSpawn() {
        super("spawn", "deamonish.command.spawn");
    }

    private ModuleMineZ mineZ = ModuleMineZ.getInstance(ModuleMineZ.class);

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.checkIsConsole();
        Player p = (Player) sender;
        MineZPlayer player = MiniGameManager.getInstance().getMiniGamePlayer(p);
        player.setPlaying(false);
        player.removeMetadata("target");
        p.setLevel(0);
        p.setExp(0);
        MiniGameManager.getInstance().removePlayerOnMiniGame(player, player.getMiniGame());
        p.getInventory().setArmorContents(null);
        p.getInventory().clear();
        p.teleport(mineZ.getLobbySpawnPoint());
        return false;
    }
}
