package ua.deamonish.modulesystem.util;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Config extends YamlConfiguration {

	public static final String SEP = File.separator; // separator - длинное слово!!
	private String path;
	private Plugin plugin;
	private boolean first = false;
	private String description = "";

	/**
	 * Загрузить конфиг со строки
	 * @param data дата конфига
	 */
	public Config(String data) {
		try {
			this.loadFromString(data);
		} catch(InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param plugin плагин
	 * @param path путь, относительно директории плагина
	 */
	public Config(Plugin plugin, String path) {
		this.plugin = plugin;
		this.path = path;
		this.reload();
	}

	public void save() {
		try {
			FileOutputStream stream = new FileOutputStream(new File(plugin.getDataFolder() + SEP + path));
			if (!description.isEmpty()) {
				stream.write(('#' + description.replace("\n", "\n#") + "\n").getBytes());
			}
			stream.write(this.saveToString().getBytes());
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reload() {
		File file = new File(plugin.getDataFolder() + SEP + path);
		if (!file.exists()) {
			try {
				first = true;
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			first = false;
		}
		try {
			FileInputStream stream = new FileInputStream(file);
			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			stream.close();
			String content = new String(bytes);
			String data = "";
			this.description = "";
			for (String line : content.split("\n")) {
				if (!line.isEmpty()) {
					if(line.charAt(0) == '#') {
						this.description += line.substring(1) + "\n";
					} else {
						data += line + "\n";
					}
				}
			}
			this.description = StringUtil.removeLast(this.description, 1);
			this.loadFromString(data);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Вставить комментарий кофига<br>
	 * Он будет виден в самом верху конфига<br>
	 * @param description строка с описанием, можно использовать переносы - '\n'
	 */
	public void setDescription(String description) {
		if (!this.description.equals(description)) {
			this.description = description;
			this.save();
		}
	}

	/**
	 * Получить или вставить новое значение в конфиг
	 * @param path путь
	 * @param def значение по уполчанию
	 * @param <T>
	 * @return значние их конфига или значение по умолчанию
	 */
	public <T> T getOrSet(String path, T def) {
		if (!this.contains(path)) {
			this.setAndSave(path, def);
			return def;
		} else {
			return (T) this.get(path);
		}
	}

	/**
	 * Установить сначение, если его не существует
	 * @param path
	 * @param value
	 */
	public Object setIfNotExist(String path, Object value) {
		if (!this.contains(path)) {
			this.setAndSave(path, value);
			return value;
		} else {
			return this.get(path);
		}
	}

	public void setDefault(String path, Object value) {
		if (first) {
			this.setAndSave(path, value);
		}
	}

	/**
	 * Редактировать и сохранить конфиг
	 * @param path
	 * @param value
	 */
	public void setAndSave(String path, Object value) {
		this.set(path, value);
		this.save();
	}

	@Override
	public void set(String path, Object value) {
		super.set(path, this.fixObject(value));
	}

	private Object fixObject(Object value) {
		if (value instanceof Location) {
			return toString((Location) value);
		} else if (value instanceof ItemStack) {
			return (ItemStack) value;

		} else if (value instanceof Material) {
			return ((Material)value).name();
		} else if (value instanceof Collection) {
			Collection<?> collectValue = Collection.class.cast(value);
			return collectValue.stream()
					.map(this::fixObject)
					.collect(Collectors.toList());
		}
		return value;
	}


	/** Удаление указаного пути в конфигурации.
	 * WARNING: Чувствительно к регистру.
	 *
	 * @param path - путь/
	 */
	public void removeAndSave(String path) {
		this.remove(path);
		this.save();
	}
	public void remove(String path){
		this.set(path, null);
	}

	/**
	 * Возвращает строку в формате
	 * material:data:amount:DisplayName:Lore:enchant,enchant
	 * если отсутствует параметр, то пишется none
	 * @param item ItemStack
	 * @return String
	 */
	public static String toString(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		String code = item.getType().name().toLowerCase()
				+ ":" + item.getDurability()
				+ ":" + item.getAmount()
				+ ":" + (meta.getDisplayName() == null ? "none" : meta.getDisplayName().replace(":", "<<>>"))
				+ ":";
		boolean first = true;
		if (meta.getLore() != null) {
			for (String lore : meta.getLore()) {
				if (first) {
					first = false;
				} else {
					code += "\\n";
				}
				code += lore.replace(":", "<<>>");
			}
		} else {
			code += "none";
		}
		code += ":";
		first = true;
		if (meta.getEnchants().size() != 0) {
			for (Enchantment enchantment : meta.getEnchants().keySet()) {
				if (first) {
					first = false;
				} else {
					code += ",";
				}
				code += enchantment.getName().toLowerCase() + "*" + meta.getEnchantLevel(enchantment);
			}
		} else {
			code += "none";
		}
		return code;
	}

	public String getStringColor(String path) {
		String line = this.getString(path);
		if (line == null) {
			return null;
		}
		line = StringUtil.replace(line, "&", "§");
		line = StringUtil.replace(line, "\\n", "\n");
		return line;
	}

	public List<String> getStringListColor(String path) {
		List<String> list = this.getStringList(path);
		if (list == null) {
			return null;
		}
		ArrayList<String> listNew = new ArrayList<>();
		for (String line : list) {
			line = StringUtil.replace(line, "&", "§");
			line = StringUtil.replace(line, "\\n", "\n");
			listNew.add(line);
		}
		return listNew;
	}

	public Location getLocation(String path) {
		String code = this.getString(path);
		return toLocation(code);
	}

	public Vector getVector(String path) {
		String code = this.getString(path);
		return toVector(code);
	}

	public ItemStack getItemStack(String path) {
		Object defLoad = this.get(path);
		if (defLoad instanceof ItemStack) {
			return (ItemStack) defLoad;
		} else {
			String code = (String) defLoad;
			return toItemStack(code);
		}
	}

	public static ItemStack toItemStack(String code) {
		if (code == null || code.equals("null") || code.length() == 0)
			return null;

		String item_s[] = code.split(":");
		if (item_s.length == 0) {
			throw new IllegalFormatCodePointException(0);
		}
		if (item_s.length == 1) {
			return new ItemStack(toMaterial(item_s[0]));
		}
		if (item_s.length == 2) {
			return new ItemStack(
					toMaterial(item_s[0]),
					Integer.parseInt(item_s[1]),
					(short) 0
			);
		}
		ItemStack item = new ItemStack(
				toMaterial(item_s[0]),
				Integer.parseInt(item_s[2]),
				Short.parseShort(item_s[1])
		);
		if (item_s.length > 3) {
			ItemMeta meta = item.getItemMeta();
			if (!item_s[3].equals("none"))
				meta.setDisplayName(item_s[3].replace("<<>>", ":"));
			if (item_s.length > 4) {
				if (!item_s[4].equals("none")) {
					ArrayList<String> lores = new ArrayList<>();
					Collections.addAll(lores, item_s[4].split("\\n"));
					meta.setLore(lores);
				}
				if (item_s.length > 5) {
					if (!item_s[5].equals("none")) {
						for (String enchant : item_s[5].split(",")) {
							String sett[] = enchant.split("\\*");
							Enchantment enchantment = null;
							try {
								enchantment = Enchantment.getById(Integer.parseInt(sett[0]));
							} catch (Exception e) {
								enchantment = Enchantment.getByName(sett[0].toUpperCase());
							}
							meta.addEnchant(enchantment, Integer.parseInt(sett[1]), false);
						}
					}
				}
				if (item_s.length > 6)
				{
					if (!item_s[6].equals("none")) {
						PotionMeta metaP = (PotionMeta) meta;
						String potion = item_s[6];
						String sett[] = potion.split("\\*");
						PotionType type;
						try {
							type = PotionType.getByDamageValue(Integer.parseInt(sett[0]));
						} catch(Exception e) {
							type = PotionType.valueOf(sett[0].toUpperCase());
						}
						try {
							switch(sett[1]) {
								case "extended":
									metaP.setBasePotionData(new PotionData(type, true, false));
									break;
								case "upgraded":
									metaP.setBasePotionData(new PotionData(type, false, true));
									break;
								default:
									metaP.setBasePotionData(new PotionData(type, false, false));
									break;
							}
						} catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
							// если нет такого аргумента
							metaP.setBasePotionData(new PotionData(type, false, false));
						}
						meta = metaP;
					}
				}
			}
			item.setItemMeta(meta);
		}
		return item;
	}

	public static String toString(Location loc) {
		return loc.getWorld().getName()
				+ ":" + loc.getX()
				+ ":" + loc.getY()
				+ ":" + loc.getZ()
				+ ":" + loc.getYaw()
				+ ":" + loc.getPitch();
	}

	public static Location toLocation(String code) {
		if (code == null || code.equals(""))
			return null;

		String loc[] = code.split(":");
		if (loc.length != 6)
			return null;

		return new Location(
				Bukkit.getWorld(loc[0]),
				Double.parseDouble(loc[1]),
				Double.parseDouble(loc[2]),
				Double.parseDouble(loc[3]),
				Float.parseFloat(loc[4]),
				Float.parseFloat(loc[5])
		);
	}

	public static Vector toVector(String code){
		if(code == null || code.equals("")) { return null; }

		String[] cords = code.split(":");
		if(cords.length != 3){
			return null;
		}

		return new Vector(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]), Integer.parseInt(cords[2]));
	}

	public Material getMaterial(String path) {
		String mat = this.getString(path);
		return toMaterial(mat);

	}
	public static Material toMaterial(String code) {

		if(code == null) {
			return null;
		}

		try {
			Material material = Material.getMaterial(code.toUpperCase());
			return material == null ? Material.getMaterial(Integer.parseInt(code)) : material;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<ItemStack> getItemStackList(String path) {
		List<ItemStack> list = new ArrayList<>();
		for(Object o : this.getList(path)) {
			if (o instanceof String) {
				list.add(toItemStack((String) o));
			} else {
				list.add((ItemStack) o);
			}
		}
		return list;
	}

	public List<Location> getLocationList(String path) {
		ArrayList<Location> items = new ArrayList<>();
		for (String string : this.getStringList(path)) {
			items.add(toLocation(string));
		}
		return items;
	}

	/**
	 * Генерировать свободный путь
	 * @param path путь, к которому будем генерировать новую секцию
	 */
	public int generateNumberPath(String path) {
		for (int i = 0;; i++)  {
			if (!this.contains(path + "." + i)) {
				return i;
			}
		}
	}

	public float getFloat(String s) {
		return (float) this.getDouble(s);
	}

	/**
	 * Получить все параметры в указанной секции
	 * @param section путь к секции
	 * @return список всех параметров из этой секции
	 */
	public Set<String> getKeys(String section) {
		return this.getConfigurationSection(section).getKeys(false);
	}
}
