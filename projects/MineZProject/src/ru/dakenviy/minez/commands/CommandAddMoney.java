package ru.dakenviy.minez.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ua.deamonish.modulesystem.command.Command;

import java.util.List;

public class CommandAddMoney extends Command {
    public CommandAddMoney() {
        super("addmoney", "deamonish.command.admin");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        if (args.length == 1) {
            ModuleMineZ.getEconomy().depositPlayer(p, Double.parseDouble(args[0]));
            return false;
        }
        if (args.length == 2) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                p.sendMessage("§cИгрок с ником " + args[0] + " оффлайн.");
                return false;
            }
            double money = Double.parseDouble(args[1]);
            if (money <= 0) {
                p.sendMessage("§cНекорректный 2-й аргумент.");
                return false;
            }
            ModuleMineZ.getEconomy().depositPlayer(player, money);
        }
        return false;
    }
}
