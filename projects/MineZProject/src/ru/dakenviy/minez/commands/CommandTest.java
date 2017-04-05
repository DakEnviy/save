package ru.dakenviy.minez.commands;

import net.minecraft.server.v1_10_R1.EntityZombie;
import net.minecraft.server.v1_10_R1.WorldServer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ua.deamonish.modulesystem.modules.command.Command;
import ua.deamonish.modulesystem.modules.wrappers.WrapperPlayServerSpawnEntityLiving;

import java.util.List;

public class CommandTest extends Command {

    public CommandTest() {
        super("test", null);
    }

    private ModuleMineZ mineZ = ModuleMineZ.getInstance();

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
       /* ArmorStand stand = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
        stand.setArms(true);
        stand.setItemInHand(new ItemStack(Material.IRON_SWORD));*/
       /*UUID uuid = p.getUniqueId();
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        Entity ent = server.a(uuid);
        org.bukkit.entity.Entity entity = ent.getBukkitEntity();
        Bukkit.broadcastMessage(entity.getName());*/
        p.setPlayerTime(18000, Boolean.parseBoolean(args[0]));
       // Bukkit.getScheduler().scheduleSyncRepeatingTask(mineZ.getPlugin(), ()->{;});

        /*ItemStack stack = Config.toItemStack(args[0]);
        p.getInventory().addItem(stack);*/
       /* if(args[0].equals("1")) {
            p.setExp(Float.parseFloat(args[1]));
            Bukkit.broadcastMessage("p.getExp(); : " + p.getExp());
            Bukkit.broadcastMessage("p.getExpToLevel() :" + p.getExpToLevel());
            Bukkit.broadcastMessage("p.getTotalExperience() :" + p.getTotalExperience());
        }
        if(args[0].equals("2")) {
            p.setTotalExperience(Integer.parseInt(args[1]));
            Bukkit.broadcastMessage("p.getExp(); : " + p.getExp());
            Bukkit.broadcastMessage("p.getExpToLevel() :" + p.getExpToLevel());
            Bukkit.broadcastMessage("p.getTotalExperience() :" + p.getTotalExperience());
        }*/

        return false;
    }

    private void startFullMoon(Player p){
        WorldServer worldServer = ((CraftWorld) p.getWorld()).getHandle();
        EntityZombie giant = new EntityZombie(worldServer);
        giant.setSize(giant.width * 6.0F, giant.length * 6.0F);
        giant.setLocation(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), p.getLocation().getYaw(), p.getLocation().getPitch());

        worldServer.addEntity(giant, CreatureSpawnEvent.SpawnReason.CUSTOM);
        WrapperPlayServerSpawnEntityLiving entityLiving = new WrapperPlayServerSpawnEntityLiving();
        entityLiving.setUniqueId(giant.getUniqueID());
        entityLiving.setX(p.getLocation().getX());
        entityLiving.setY(p.getLocation().getY());
        entityLiving.setZ(p.getLocation().getZ());
        entityLiving.setEntityID(giant.getBukkitEntity().getEntityId());
        entityLiving.setType(EntityType.GIANT);
        entityLiving.sendPacket(p);
        giant.setSize(giant.width * 6.0F, giant.length * 6.0F);
    }
}
