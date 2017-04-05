package ua.deamonish.modulesystem.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class ScoreBoardUtil {
    private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static void sendTeam(Player player, String name, String displayName,
                                String prefix, String suffix, Collection<String> players) {
        sendTeam0(player, name, displayName, prefix, suffix, players, 0);
    }

    public static void updateTeam(Player player, String name, String displayName,
                                  String prefix, String suffix, Collection<String> players) {
        sendTeam0(player, name, displayName, prefix, suffix, players, 2);
    }

    public static void updateTeam(Collection<Player> send, String name, String displayName,
                                  String prefix, String suffix, Collection<String> players) {
        sendTeam0(send, name, displayName, prefix, suffix, players, 2);
    }

    private static void sendTeam0(Player player, String name, String displayName,
                                  String prefix, String suffix, Collection<String> players, int n) {
        try {
            protocolManager.sendServerPacket(player, createPacketSendTeam(name, displayName, prefix, suffix, players, n));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void sendTeam0(Collection<Player> send, String name, String displayName,
                                  String prefix, String suffix, Collection<String> players, int n) {
        PacketContainer packet = createPacketSendTeam(name, displayName, prefix, suffix, players, n);

        send.forEach(player -> {
            try {
                protocolManager.sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static PacketContainer createPacketSendTeam(String name, String displayName,
                                                        String prefix, String suffix, Collection<String> players, int n) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        StructureModifier<String> strings = packet.getStrings();
        strings.write(0, name);
        strings.write(1, displayName);
        strings.write(2, prefix);
        strings.write(3, suffix);
        packet.getSpecificModifier(Collection.class).write(0, players);
        packet.getIntegers().write(1, n);
        return packet;
    }
}
