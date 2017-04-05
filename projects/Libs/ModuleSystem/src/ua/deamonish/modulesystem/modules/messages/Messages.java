package ua.deamonish.modulesystem.modules.messages;

import ua.deamonish.modulesystem.modules.item.ItemDescription;
import ua.deamonish.modulesystem.util.StringUtil;

import java.util.HashMap;

public class Messages {

    private Messages() {
    }

    private static HashMap<String, String> messages = new HashMap<>();
    private static HashMap<String, ItemDescription> itemLore = new HashMap<>();

    public static String getMessage(String key) {
        String message = messages.get(key);
        if(message == null){
            return "";
        } else {
            return message;
        }
    }

    public static void removeMessage(String key) {
        messages.remove(key);
    }

    public static void addMessage(String key, String message) {
        message = message.replace("&", "§");
        messages.put(key, message);
    }

    public static HashMap<String, String> getMessages() {
        return messages;
    }

    public static String getMessage(String key, String... replaced) {
        String message = messages.get(key);
        if (message != null) {
            return Messages.replaced(message, replaced);
        }
        return "";
    }

    public static HashMap<String, ItemDescription> getItemLore() {
        return itemLore;
    }

    public static ItemDescription getItemLore(String key) {
        return itemLore.get(key);
    }

    public static void addLore(String key, ItemDescription description) {
        itemLore.put(key, description);
    }

    private static String replaced(String mess, String... replaced) {
        for (int i = 0; i < replaced.length - 1; i += 2) {
            mess = StringUtil.replace(mess, replaced[i], replaced[i + 1]);
        }
        return mess;
    }
}
