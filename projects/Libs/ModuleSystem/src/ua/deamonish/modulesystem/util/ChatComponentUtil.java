package ua.deamonish.modulesystem.util;

import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.HashMap;

public class ChatComponentUtil {

	private static final HashMap<String, String> mapTypes = load0();
	private static final HashMap<String, String> mapColor = load1();

	private static HashMap<String, String> load0() {
		HashMap<String, String> map = new HashMap<>();
		map.put("obfuscated", "k");
		map.put("bold", "l");
		map.put("strikethrough", "m");
		map.put("underlined", "n");
		map.put("italic", "o");
		return map;
	}

	private static HashMap<String, String> load1() {
		HashMap<String, String> map = new HashMap<>();
		map.put("black", "0");
		map.put("dark_blue", "1");
		map.put("dark_green", "2");
		map.put("dark_aqua", "3");
		map.put("dark_red", "4");
		map.put("dark_purple", "5");
		map.put("gold", "6");
		map.put("gray", "7");
		map.put("dark_gray", "8");
		map.put("blue", "9");
		map.put("green", "a");
		map.put("aqua", "b");
		map.put("red", "c");
		map.put("light_purple", "d");
		map.put("yellow", "e");
		map.put("white", "f");
		return map;
	}

	/**
	 * Достать из серилизированого ChatComponent'a текст сохраняя цвета
	 * @param json json дата
	 * @return текст
	 */
	public static String jsonToText(String json) {
		if (json.length() < 33) {
			return "";
		}
		json = json.substring(10, json.length() - 12);
		String newLine = "";
		char value[] = json.toCharArray();

		int posStart = -1;
		String read = "";
		boolean readText = false;
		for (int pos = 0; pos < value.length; pos++) {
			if (value[pos] == '\"') {
				if (posStart == -1) {
					posStart = pos + 1;
				} else {
					char[] temp = new char[pos - posStart];
					System.arraycopy(value, posStart, temp, 0, temp.length);
					posStart = -1;

					String text = new String(temp);
					if (readText) {
						newLine += (newLine.length() > 0 ? "§r" : "") + read + text;
						read = "";
						readText = false;
					} else if (text.equals("text")) {
						readText = true;
					} else {
						String color = mapColor.get(text);
						if (color != null) {
							read = "§" + color + read;
						} else {
							String type = mapTypes.get(text);
							if (type != null) {
								read = read + "§" + type;
							}
						}
					}
				}
			}
		}
		return StringEscapeUtils.unescapeJava(newLine);
	}

	/**
	 * Преобразовать текст в json, как бы это сделал ChatComponent
	 * @param text текст
	 * @return json
	 */
	public static String textToJson(String text) {
		text = fixText(text);
		return "{\"extra\":[{\"text\":\"" + text + "\"}],\"text\":\"\"}";
	}

	/**
	 * Исправить тектс так, чтобы после вставки в json он не нарушил синтаксис
	 * @param text текст
	 * @return фикс текста
	 */
	public static String fixText(String text) {
		return (text = JSONParser.quote(text)).substring(1, text.length() - 1);
	}
}
