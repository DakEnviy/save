package ru.dakenviy.minez.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ua.deamonish.modulesystem.modules.command.Command;

import java.util.HashSet;
import java.util.List;

public class CommandSetChest extends Command {

    private ModuleMineZ mineZ = ModuleMineZ.getInstance(ModuleMineZ.class);

    public CommandSetChest() {
        super("setchest", "deamonish.command.admin");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.checkIsConsole();
        Player p = (Player) sender;
        /*if(args.length == 0){
            p.sendMessage("§b/setchest - смотря на блок сундука. Добавит сундук в миниигру.");
            return true;
        }*/
        Block block = p.getTargetBlock((HashSet<Byte>) null, 20);
        if (block == null || !block.getType().equals(Material.CHEST)) {
            p.sendMessage("§cНе найден сундук, по взгляду игрока.");
            return false;
        }
        mineZ.addChest(block.getLocation());
        sender.sendMessage("§aВы успешно добавили сундук. Он " + mineZ.getChestsNumerable() + " по счету. \n§bПосле окончания добавления сундуков. Настройте конфиг, и перезапустите сервер.");

        return false;
    }
}
