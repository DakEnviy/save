package ua.deamonish.modulesystem.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class ChatComponent {

	private String message = "[\"\""; // Это должно быть сначала

	public ChatComponent() {

	}

	public ChatComponent(String text) {
		this.addText(text);
	}

	/**
	 * Добавить текст.
	 * Если по нему кликнуть, то выполнится команда от имени игрока
	 * @param text - текст, по которому можно кликнуть.
	 * @param command - команда, которую нужно выполнить при нажатии.
	 */
	@Deprecated
	public ChatComponent addTextClick(String text, String command) {
		this.message += ",{\"text\":\"";
		this.message += text;
		this.message += "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"";
		this.message += command;
		this.message += "\"}}";
		return this;
	}

	/**
	 * Добавить текст.
	 * Если на него навести, то выведется текст (перенос \n)
	 * @param text - текст, на которомй можно навести.
	 * @param hover - текст, что отобразится при наведении.
	 */
	@Deprecated
	public ChatComponent addTextHover(String text, String hover) {
		this.message += ",{\"text\":\"";
		this.message += text;
		this.message += "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"";
		this.message += hover;
		this.message += "\"}}";
		return this;
	}

	/**
	 * Добавить текст.
	 * Если на него навести, то выведется текст (перенос \n)
	 * а если нажать, то выполнится команда
	 * @param text - текст, на которомй можно навести и нажать.
	 * @param hover - текст, что отобразится при наведении.
	 * @param command - команда, которую нужно выполнить при нажатии.
	 */
	@Deprecated
	public ChatComponent addTextClickAndHover(String text, String hover, String command) {
		this.message += ",{\"text\":\"";
		this.message += text;
		this.message += "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"";
		this.message += command;
		this.message += "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"";
		this.message += hover;
		this.message += "\"}}";
		return this;
	}
	@Deprecated
	public ChatComponent addTextSuggestAndHover(String text, String hover, String suggest) {
		this.message += ",{\"text\":\"";
		this.message += text;
		this.message += "\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"";
		this.message += suggest;
		this.message += "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"";
		this.message += hover;
		this.message += "\"}}";
		return this;
	}

	/**
	 * Добавить текст
	 * @param text текст
	 * @param hover что делать, если навел на текст
	 * @param valueHover значение, когда навел на текст
	 * @param click что делать, если кликнул по тексту
	 * @param valueClick значение, когда кликгул по тексту
	 * @return this
	 */
	public ChatComponent addText(String text, HOVER hover, Object valueHover, CLICK click, Object valueClick) {
		text = ChatComponentUtil.fixText(text);
		this.message += ",{\"text\":\"" + text + "\"";
		if (hover != null) {
			this.message += "," + hover.get(valueHover);
		}
		if (click != null) {
			this.message += "," + click.get(valueClick);
		}
		this.message += "}";
		return this;
	}

	/**
	 * Добавить текст
	 * @param text текст
	 * @param hover что делать, если навел на текст
	 * @param valueHover значение, когда навел на текст
	 * @return this
	 */
	public ChatComponent addText(String text, HOVER hover, Object valueHover) {
		this.addText(text, hover, valueHover, null, null);
		return this;
	}

	/**
	 * Добавить текст
	 * @param text текст
	 * @param click что делать, если кликнул по тексту
	 * @param valueClick значение, когда кликгул по тексту
	 * @return this
	 */
	public ChatComponent addText(String text, CLICK click, Object valueClick) {
		this.addText(text, null, null, click, valueClick);
		return this;
	}

	/**
	 * Добавить текст
	 * @param text текст
	 */
	public ChatComponent addText(String text) {
		this.addText(text, null, null, null, null);
		return this;
	}

	/**
	 * Заменить текст
	 */
	public ChatComponent replace(String s1, String s2) {
		ChatComponent chatComponent = new ChatComponent();
		chatComponent.message = this.message.replace(s1,s2);
		return chatComponent;
	}

	/**
	 * Отправить сообщение игроку
	 */
	public void send(CommandSender player) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " " + message + "]");
	}

	public String getMessage() {
		return message;
	}

	public static ChatComponent getErrorReport(Exception e, String text) {
		e.printStackTrace();
		ChatComponent message = new ChatComponent();
		String error = e.getClass().getName();
		if (e.getMessage() != null) {
			for (String err : StringUtil.stack(e.getMessage(), 100)) {
				error += "§4" + err + "\n";
			}
		}
		int limit = 0;
		for (StackTraceElement element : e.getStackTrace()) {
			String elem = element.toString();
			if (StringUtil.contains(elem, "ua.deamonish")) {
				error += "§c at    " + element + "\n";
			} else {
				error += "§7 at    " + element + "\n";
			}
			if (limit++ > 27) {
				error += "§7(Ну там еще ошибки...)";
				break;
			}
		}
		message.addTextHover(text, error);
		return message;
	}

	/**
	 * Варианты событий, что возможны при нажатии.
	 */
	public enum CLICK {
		open_url,
		open_file,
		run_command,
		suggest_command,
		change_page,
		;
		public String get(Object o) {
			String text = o.toString();
			text = ChatComponentUtil.fixText(text);
			return "\"clickEvent\":{\"action\":\"" + this.name() + "\",\"value\":\"" + text + "\"}";
		}
	}

	/**
	 * Варианты событий, что возможны при наведении.
	 */
	public enum HOVER {
		show_text,
		show_achievement,
		show_item,
		show_entity,
		;

		public String get(Object o) {
			String text = o.toString();
			text = ChatComponentUtil.fixText(text);
			return "\"hoverEvent\":{\"action\":\"" + this.name() + "\",\"value\":\"" + text + "\"}";
		}
	}


}
