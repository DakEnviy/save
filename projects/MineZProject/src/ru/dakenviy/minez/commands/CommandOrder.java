package ru.dakenviy.minez.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.event.player.PlayerOrderEvent;
import ru.dakenviy.minez.minez.MineZMiniGame;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ru.dakenviy.minez.player.MineZPlayer;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.modulesystem.command.Command;

import java.util.List;

public class CommandOrder extends Command {
    public CommandOrder() {
        super("order", null, "orders");
    }

    private int minimalOrder = ModuleMineZ.getInstance().getConfig().getInt("minimal_order_bet");
    private MiniGameManager manager = MiniGameManager.getInstance();

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.checkIsConsole();
        Player p = (Player) sender;
        MineZPlayer customer = manager.getMiniGamePlayer(p);
        if (args.length == 0) {
            p.sendMessage("§3/order [PlayerName] [bets] - §bзаказать убийство игрока, за указаную сумму.");
            return false;
        }
        if (args.length == 2) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player != null) {
                MineZPlayer victim = manager.getMiniGamePlayer(player);
                if (victim.getMiniGame().equals(customer.getMiniGame())) {
                    int bet = Integer.parseInt(args[1]);
                    if (bet >= minimalOrder) {
                        MineZMiniGame game = (MineZMiniGame) customer.getMiniGame();
                        Bukkit.getPluginManager().callEvent(new PlayerOrderEvent(victim, customer, bet));
                    } else {
                        p.sendMessage("§bВы указали ставку за голову игрока, меньше минимальной. §eМинимальная ставка - §b" + minimalOrder);
                        return false;
                    }
                } else {
                    p.sendMessage("§cИгрок с ником §b" + args[0] + " §cнаходится в другой миниигре. Его нельзя заказать.");
                    return false;
                }
            } else {
                p.sendMessage("§cИгрок с ником §b" + args[0] + " §cоффлайн. Его убийство нельзя заказать.");
                return false;
            }
        }
        return false;
    }
}
