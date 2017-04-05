package ru.dakenviy.minez.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.dakenviy.minez.zombie.MineZZombie;

import java.lang.reflect.Field;
import java.util.*;

public class Util
{
    public static final Random rand;

    public static boolean randomChance(final Integer likelihood) {
        final Double d = likelihood / 100.0;
        return d > Math.random();
    }

    public static Double getDistanceXZ(final int x, final int z, final int x1, final int z1) {
        final int a = (x1 - x) * (x1 - x);
        final int b = (z1 - z) * (z1 - z);
        return Math.sqrt(a + b);
    }

    public static String concatRestList(final List<String> list, final int start) {
        String result = "";
        for (int i = start; i < list.size(); ++i) {
            result = result + list.get(i) + " ";
        }
        return result.substring(0, result.length() - 1);
    }

    public static String concatRestArray(final String[] ar, final int start) {
        String result = "";
        for (int i = start; i < ar.length; ++i) {
            result = result + ar[i] + " ";
        }
        return result.substring(0, result.length() - 1);
    }

    public static int constrainRange(final int i, final int min, final int max) {
        return Math.max(min, Math.min(i, max));
    }

    public static int tryParseInt(final String s, final int def) {
        try {
            return Integer.parseInt(s);
        }
        catch (Exception var3) {
            return def;
        }
    }

    public static boolean tryParseBoolean(final String s, final boolean def) {
        try {
            return Boolean.parseBoolean(s);
        }
        catch (Exception var3) {
            return def;
        }
    }


    public static ItemStack[] getRepairItems(final CraftingInventory ci) {
        final ItemStack[] twoRepair = new ItemStack[2];
        final ItemStack[] contents = ci.getContents();
        Boolean foundFirst = false;
        for (int i = 1; i < contents.length; ++i) {
            if (isTool(contents[i])) {
                if (foundFirst) {
                    twoRepair[1] = contents[i];
                }
                else {
                    twoRepair[0] = contents[i];
                    foundFirst = true;
                }
            }
        }
        return twoRepair;
    }

    public static boolean isEmpty(final Inventory i) {
        for (final ItemStack is : Arrays.asList(i.getContents())) {
            if (is != null && is.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    public static void spawnZombies(final Location l, final int tier) {
        final int maxAmt = 2 + tier;
        final int maxRange = 10 + 5 * tier;
        final int maxNodes = tier + 3;
        final int nodeAmt = (int)(Object) (Math.random() * maxNodes);
        for (int i = 0; i < nodeAmt; ++i) {
            final int amt = (int)(Object) (Math.random() * maxAmt) + 1;
            final int x = l.getBlockX() + (int)(Object) (Math.random() * maxRange);
            final int z = l.getBlockZ() + (int)(Object) (Math.random() * maxRange);
            final Location spawn = findOpenSpace(l.getWorld(), x, z, l.getBlockY() - 20);
            if (spawn != null) {
                for (int j = 0; j < amt; ++j) {
                    MineZZombie.spawn(spawn);
                }
            }
        }
    }

    public static Location findOpenSpace(final World w, final int x, final int z, final int baseY) {
        Boolean found = false;
        int y = 0;
        while (!found) {
            if (w.getBlockAt(new Location(w, (double)x, (double)(baseY + y), (double)z)).getType() == Material.AIR && w.getBlockAt(new Location(w, (double)x, (double)(baseY + y + 1), (double)z)).getType() == Material.AIR) {
                return new Location(w, (double)x, (double)(baseY + y), (double)z);
            }
            if (y > 40) {
                found = true;
            }
            ++y;
        }
        return null;
    }

    public static ArrayList<Player> getNearbyPlayers(final Location l, final int thresh) {
        final List<Player> playerList = l.getWorld().getPlayers();
        final int thresh2 = thresh * thresh;
        final ArrayList<Player> nearbyPlayers = new ArrayList<>();
        for (Player p : playerList) {
            if (l.distanceSquared(p.getLocation()) < thresh2) {
                nearbyPlayers.add(p);
            }
        }
        return (ArrayList<Player>)nearbyPlayers;
    }

    public static boolean areSameEnchants(final ItemStack is, final ItemStack is2) {
        Map encMap = is.getEnchantments();
        for (Object o : encMap.keySet()) {
            Enchantment e = (Enchantment) o;
            if (is2.getEnchantmentLevel(e) != is.getEnchantmentLevel(e)) {
                return false;
            }
        }
        encMap = is2.getEnchantments();
        for (Object o : encMap.keySet()) {
            Enchantment e = (Enchantment) o;
            if (is2.getEnchantmentLevel(e) != is.getEnchantmentLevel(e)) {
                return false;
            }
        }
        return true;
    }



    public static String getItemName(final ItemStack s) {
        return (s == null) ? "" : ((s.getItemMeta() == null) ? "" : (s.getItemMeta().hasDisplayName() ? s.getItemMeta().getDisplayName() : ""));
    }

    public static void renameItem(final ItemStack s, final String name) {
        final ItemMeta im = s.getItemMeta();
        im.setDisplayName(name);
        s.setItemMeta(im);
    }

    public static boolean isHookLanded(final Fish hook) {
        if (hook.getVelocity().length() > 1.2) {
            return false;
        }
        final Location loc = hook.getLocation();
        final World w = hook.getWorld();
        return w.getBlockAt(loc.add(0.0, -0.25, 0.0)).getType().isSolid() || w.getBlockAt(loc.add(0.0, -0.25, 0.0)).getType() == Material.GRASS || w.getBlockAt(loc.add(0.25, -0.25, 0.0)).getType().isSolid() || w.getBlockAt(loc.add(-0.25, -0.25, 0.0)).getType() == Material.GRASS || (w.getBlockAt(loc.add(0.0, -0.25, 0.25)).getType().isSolid() || w.getBlockAt(loc.add(0.0, -0.25, -0.25)).getType() == Material.GRASS);
    }

    public static Double randomRange(final Integer low, Integer high) {
        Double result = Util.rand.nextDouble();
        result *= (double)(high - low);
        result += (double)low;
        return result;
    }

    public static Double randomRangeNormal(final Integer low, final Integer high) {
        Double randMult = Util.rand.nextGaussian() * 0.8;
        randMult = Math.min(1.0, randMult);
        randMult = Math.max(-1.0, randMult);
        Double avg = (high == 0) ? 0.0 : ((low + high) / 2.0);
        avg += randMult * avg / 2.0;
        return avg;
    }

    public static void modifyMaxStack(final Item item, final int amount) {
        try {
            final Field e = Item.class.getDeclaredField("maxStackSize");
            e.setAccessible(true);
            e.setInt(item, amount);
        }
        catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public static void makeModifiable(final Field nameField) throws Exception {
        nameField.setAccessible(true);
        int modifiers = nameField.getModifiers();
        final Field modifierField = nameField.getClass().getDeclaredField("modifiers");
        modifiers &= 0xFFFFFFEF;
        modifierField.setAccessible(true);
        modifierField.setInt(nameField, modifiers);
    }

    public static boolean isTool(final ItemStack is) {
        final Material m = is.getType();
        final String name = m.name();
        return name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS") || name.contains("PANTS") || name.contains("PICKAXE") || name.contains("AXE") || name.contains("HOE") || name.contains("SPADE") || name.contains("SWORD") || name.contains("BOW");
    }

    public static void setDurability(final ItemStack is, final Integer percent) {
        final Float porcien = (100 - percent) / 100.0f;
        is.setDurability((short)(is.getType().getMaxDurability() * porcien));
    }

    static {
        rand = new Random();
    }
}
