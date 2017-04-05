package ru.dakenviy.minez.commands;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.minez.MineZMiniGame;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.modulesystem.modules.command.Command;

import java.util.HashSet;
import java.util.List;

/**
 * Команда, что связывает по взгляду на табличку, миниигру с табличкой.
 * Что бы игроки кликая по таблице, телепортировались в мир миниигры.
 */
public class CommandSetMiniGameTable extends Command {

    public CommandSetMiniGameTable() {
        super("setminigame", "deamonish.command.admin");
    }

    private ModuleMineZ mineZ = ModuleMineZ.getInstance(ModuleMineZ.class);

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        sender.sendMessage("§eСписок зарагестрированых таблицы: " + StringUtils.join(mineZ.getAvailableSign().values(), " "));
        sender.sendMessage("§eСписок загруженных миров: " + StringUtils.join(Bukkit.getWorlds(), " "));
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.checkIsConsole();
        Player p = (Player) sender;
        if (args.length == 0) {
            p.sendMessage("§b/setminigame §3[имя миниигры]§b. При этом вы должны смотреть на табличку.");
            p.sendMessage("§bЭта команда закрепит за табличкой миниигру.");
            return false;
        }

        if (args.length == 1) {
            Block block = p.getTargetBlock((HashSet<Byte>) null, 20);
            if (block == null || !block.getType().equals(Material.WALL_SIGN)) {
                p.sendMessage("§cНе найдена табличка, по взгляду игрока.");
                return false;
            }

            MineZMiniGame game = (MineZMiniGame) MiniGameManager.getInstance().getMiniGame(args[0]);
            if (game != null && mineZ.getAvailableSign().values().contains(game)) {
                p.sendMessage("§cМиниигра " + game + " уже привязана к табличке.");
                return false;
            } else if (game == null) {
                // Создаем новую миниигру.
                game = new MineZMiniGame(args[0], ModuleMineZ.getMaxPlayerOnMiniGame(), mineZ.getPlugin());
                MiniGameManager.getInstance().addMiniGame(game);
                p.sendMessage("§aВы создали новую миниигру с названием " + game.getMiniGameName());
            }
            Sign sign = (Sign) block.getState();
            mineZ.getAvailableSign().put(sign, game);
            mineZ.setSign(sign, game);
            mineZ.saveSigns();
            mineZ.updateSign();
            p.sendMessage("§aВы успешно привязали миниигру к табличке.");
        }
        return false;
    }
}
