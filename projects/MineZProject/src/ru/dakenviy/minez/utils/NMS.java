package ru.dakenviy.minez.utils;

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Collection;
import java.util.List;

public class NMS {
    private static EnumParticle[] values = EnumParticle.values();
    public static void sendActionBar(Player player, String message) {
        CraftPlayer p = (CraftPlayer) player;
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        p.getHandle().playerConnection.sendPacket(ppoc);
    }

    public static ArmorStand spawnArmorStand(Location loc){
        WorldServer world = ((CraftWorld)loc.getWorld()).getHandle();
        EntityArmorStand stand = new EntityArmorStand(world, loc.getX(), loc.getY(), loc.getZ());
        world.addEntity(stand, CreatureSpawnEvent.SpawnReason.CUSTOM);
        stand.setInvulnerable(true);
        stand.dead = false;
        return (ArmorStand) stand.getBukkitEntity();
    }

    public static void sendParticle(Collection<Player> players, Particle particle, boolean b, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double speed, int... parameters) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(values[particle.ordinal()], b, (float) x, (float) y, (float) z, (float) offsetX, (float) offsetY, (float) offsetZ, (float) speed, count, parameters);
        for(Player player : players) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }
    public static void sendTitle(Player player, String textTitle, String textSubtitle, int a, int b, int c) {
        if(textTitle == null) {
            textTitle = "";
        }
        if(textSubtitle == null) {
            textSubtitle = "";
        }

        IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textTitle + "\"}");
        IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textSubtitle + "\"}");
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle subTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(a, b, c);

        PlayerConnection connection = ((org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(subTitle);
        connection.sendPacket(title);
        connection.sendPacket(length);
    }

    public static void sendTitle(List<Player> players, String textTitle, String textSubtitle){
        for(Player p : players){
            sendTitle(p, textTitle, textSubtitle);
        }
    }

    public static void sendTitle(Player player, String textTitle, String textSubtitle) {
        sendTitle(player, textTitle, textSubtitle, 10, 40, 10);
    }
}
