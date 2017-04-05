package ua.deamonish.modulesystem.util;


import org.bukkit.Bukkit;


public class BukkitClasses {

	public static final String VERSION     = Bukkit.getServer().getClass().getName().split("\\.")[3]; //версия пакетов
	public static final String OBC_VERSION = "org.bukkit.craftbukkit." + VERSION + ".";
	public static final String NMS_VERSION = "net.minecraft.server." + VERSION + ".";

	public static Class PacketPlayOutMapChunk = forName(NMS_VERSION + "PacketPlayOutMapChunk");
	public static Class PacketPlayOutBlockChange = forName(NMS_VERSION + "PacketPlayOutBlockChange");

	public static Class Block = forName(NMS_VERSION + "Block");
	public static Class Material = forName(NMS_VERSION + "Material");
	public static Class MaterialMapColor = forName(NMS_VERSION + "MaterialMapColor");
	public static Class BlockPosition = forName(NMS_VERSION + "BlockPosition");
	public static Class EntityTypes = forName(NMS_VERSION + "EntityTypes");
	public static Class EntityLiving = forName(NMS_VERSION + "EntityLiving");

	private static Class forName(String clazz) {

		try {
			return Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			System.out.println("Класс '" + clazz + "' не найден.");
			e.printStackTrace();
			return null;
		}
	}
}
