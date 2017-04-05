package ru.dakenviy.minez.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.containers.MineZArmorStand;
import ru.dakenviy.minez.minez.MineZMiniGame;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ru.dakenviy.minez.utils.NMS;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.modules.command.Command;

import java.util.HashSet;
import java.util.List;

public class CommandSetArmorStand extends Command {

    private ModuleMineZ mineZ = Module.getInstance(ModuleMineZ.class);

    public CommandSetArmorStand() {
        super("setarmorstand", "deamonish.command.admin", "setstand");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.checkIsConsole();
        Player p = (Player) sender;
        if (args.length == 0) {
            p.sendMessage("§b/setarmorstand [name] - спавнит под вами арморстенд и создает в конфиге секцию с указаным name. \n" +
                    "§bПосле создания армор стендов, настройте конфиг и перезапустите сервер");
            return false;
        }

        if (args.length == 1) {
            Location spawn = p.getLocation().clone();

            HashSet<MineZMiniGame> miniGames = MiniGameManager.getInstance().getMiniGame();
            for (MineZMiniGame game : miniGames) {
                spawn.setWorld(game.getWorld());
                ArmorStand stand = NMS.spawnArmorStand(spawn);
                MineZArmorStand armorStand = new MineZArmorStand(stand);
                game.addStand(armorStand);
                mineZ.printDebug("Заспавнили армор стенд " + spawn);

            }
            mineZ.addArmorStand(spawn, args[0]);
            p.sendMessage("§aВы успешно добавили арморстенд.");
        }

        return false;
    }
}
