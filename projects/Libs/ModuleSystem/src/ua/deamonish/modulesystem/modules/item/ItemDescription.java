package ua.deamonish.modulesystem.modules.item;

import java.util.ArrayList;
import java.util.List;

public class ItemDescription {

    private String displayName;
    private List<String> lore = new ArrayList<>();

    public ItemDescription(String displayName, List<String> lore) {
        this.displayName = displayName;
        this.lore = lore;
    }

    public ItemDescription(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }
}
