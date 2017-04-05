package ru.dakenviy.minez.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.dakenviy.minez.event.fullmoon.FullMoonChangeEvent;
import ua.deamonish.modulesystem.command.Command;

import java.util.List;

public class CommandStartFullMoon extends Command {
    public CommandStartFullMoon() {
        super("startfullmoon", "deamonish.command.admin");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Bukkit.getPluginManager().callEvent(new FullMoonChangeEvent(true));
        return false;
    }
}
