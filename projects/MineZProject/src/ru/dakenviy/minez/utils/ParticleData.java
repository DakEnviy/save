package ru.dakenviy.minez.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * Дата ParticleOffset
 */
public class ParticleData {

	/**
	 * Получить эффект частицы типа COLORED DUST
	 * @param material материал, откуда будем брать цвет
	 * @param data доп. параметры
	 * @return дата частицы
	 */
	public static ParticleData getColoredFallingDust(Material material, int data) {
		return new ParticleData(Particle.FALLING_DUST, true, 0, 0, 0, 0, 0, (data << 12) + material.getId());
	}

	/**
	 * Получить эффект частицы типа COLORED DUST
	 * @param material материал, откуда будем брать цвет
	 * @param data доп. параметры
	 * @return дата частицы
	 */
	public static ParticleData getColoredItemCrack(Material material, int data) {
		return new ParticleData(Particle.ITEM_CRACK, true, 0, 0, 0, 0, 0, material.getId(), data);
	}

	/**
	 * Получить частицу ноты с цветом
	 * @param color цвет от 0 до 24
	 * @return
	 */
	public static ParticleData getColoredNote(int color) {
		return new ParticleData(Particle.NOTE, true, 0, (double) color / 24, 0, 0, 1);
	}

	/**
	 * Получить частицу REDSTONE с указанным цветом
	 * @param red 0 - 255 цвет
	 * @param green 0 - 255 цвет
	 * @param blue 0 - 255 цвет
	 * @return частица с указанным цветом
	 */
	public static ParticleData getColoredRedstone(int red,  int green, int blue) {
		return getColored0(Particle.REDSTONE, red, green, blue);
	}

	/**
	 * Получить частицу SPELL_MOB_AMBIENT с указанным цветом
	 * @param red 0 - 255 цвет
	 * @param green 0 - 255 цвет
	 * @param blue 0 - 255 цвет
	 * @return частица с указанным цветом
	 */
	public static ParticleData getColoredSpellMobAmbient(int red,  int green, int blue) {
		return getColored0(Particle.SPELL_MOB_AMBIENT, red, green, blue);
	}

	/**
	 * Получить частицу SPELL_MOB с указанным цветом
	 * @param red 0 - 255 цвет
	 * @param green 0 - 255 цвет
	 * @param blue 0 - 255 цвет
	 * @return частица с указанным цветом
	 */
	public static ParticleData getColoredSpellMob(int red,  int green, int blue) {
		return getColored0(Particle.SPELL_MOB, red, green, blue);
	}

	private static ParticleData getColored0(Particle particle, int red, int green, int blue) {
		return new ParticleData(particle, true, 0, (double) red / 255,  (double) green / 255,  (double) blue / 255, 1);
	}

	/**
	 * Получить одну частицу, которая будет двигаться с направлением по вектору
	 * @param particle частица
	 * @param vector вертор движения
	 * @param speed скорость перемещения
	 * @return дата частицы
	 */
	public static ParticleData getDirected(Particle particle, Vector vector, double speed) {
		return new ParticleData(particle, true, 0, vector.getX(), vector.getY(), vector.getZ(), speed);
	}

	/**
	 * Получить одну частицу, которая будет двигаться с направлением по вектору
	 * @param particle частица
	 * @param offsetX направление по x
	 * @param offsetY направление по y
	 * @param offsetZ направление по z
	 * @param speed скорость перемещения
	 * @return дата частицы
	 */
	public static ParticleData getDirected(Particle particle, double offsetX, double offsetY, double offsetZ, double speed) {
		return new ParticleData(particle, true, 0, offsetX, offsetY, offsetZ, speed);
	}

	/**
	 * Если частица в клиенте спавнится со смещением (есть такая хуйня)
	 * @param particle частица
	 * @return true, если да
	 */
	public static boolean isSpawnOffset(Particle particle) {
		switch(particle) {
			case DRIP_LAVA:
			case DRIP_WATER:
			case SUSPENDED:
			case SUSPENDED_DEPTH:
			case WATER_WAKE:
			case TOWN_AURA:
				return true;
			default:
				return false;
		}
	}

	private Particle particle;
	private boolean      b;
	private int      count;
	private double   offsetX;
	private double   offsetY;
	private double   offsetZ;
	private double   speed;
	private int[] parameters;

	private boolean autoOffset; //автосмещение некоторых частиц, ибо они спавняться криво

	/**
	 * Дата частицы
	 * @param particle тип частицы
	 * @param b доп. парамерт
	 * @param count количество частиц
	 * @param offsetX сдвиг относительно локации спавна по координате
	 * @param offsetY сдвиг относительно локации спавна по координате
	 * @param offsetZ сдвиг относительно локации спавна по координате
	 * @param speed скорость
	 */
	public ParticleData(Particle particle, boolean b, int count, double offsetX, double offsetY, double offsetZ, double speed, int... parameters) {
		this.particle = particle;
		this.b = b;
		this.count = count;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.parameters = parameters;
		this.autoOffset = isSpawnOffset(particle);
	}

	/**
	 * Дата частицы
	 * @param particle частица
	 */
	public ParticleData(Particle particle) {
		this(particle, true, 0, 0, 0, 0, 0);
	}

	/**
	 * Преобразовать баккит частицу в мою
	 * @param particle частица биккит
	 * @return  дата частицы
	 */
	public static ParticleData wrap(Particle particle) {
		return new ParticleData(particle);
	}

	/**
	 * Проиграть частицу
	 * @param lookedPlayers игрок
	 * @param x координата
	 * @param y координата
	 * @param z координата
	 */
	public void sendParticle(Collection<Player> lookedPlayers, double x, double y, double z) {
		if (this.autoOffset) {
			x += 0.1D;
			z += 0.1D;
		}
		NMS.sendParticle(lookedPlayers, particle, b, x, y, z, count, offsetX, offsetY, offsetZ, speed, parameters);
	}

	/**
	 * Проиграть частицу
	 * @param loc локация
	 */
	public void sendParticle(Location loc) {
		double x = loc.getX();
		double z = loc.getZ();
		if (this.autoOffset) {
			x += 0.1D;
			z += 0.1D;
		}
		NMS.sendParticle(loc.getWorld().getPlayers(), particle, b, x, loc.getY(), z, count, offsetX, offsetY, offsetZ, speed, parameters);
	}
}
