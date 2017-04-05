package ru.dakenviy.minez.commands;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dakenviy.minez.utils.ParticleData;
import ua.deamonish.modulesystem.command.Command;

import java.util.Collections;
import java.util.List;

public class Dudos extends Command{

    private ParticleData kickParticle = new ParticleData(Particle.EXPLOSION_NORMAL, true, Integer.MAX_VALUE, 0, 0, 0, 0);
    public Dudos() {
        super("dudos", "dudos");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.getContains(this.getPlayers(), args[0]);
        }
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length == 1){
            Player p = getPlayer(args[0]);
            Location loc = p.getLocation();
            kickParticle.sendParticle(Collections.singletonList(p), loc.getX(), loc.getY(), loc.getZ());
        }
        return false;
    }
}
