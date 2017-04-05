package ru.dakenviy.minez.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ua.deamonish.modulesystem.modules.command.Command;

import java.util.List;
import java.util.stream.Collectors;

public class Kill extends Command {
    public Kill() {
        super("kills", "test");
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
        List<LivingEntity> entityLIst = loc.getWorld().getLivingEntities().stream().filter(entity -> entity.getType() != EntityType.PLAYER).collect(Collectors
                .toList());
        entityLIst.forEach(LivingEntity::remove);
        return false;
    }
}
