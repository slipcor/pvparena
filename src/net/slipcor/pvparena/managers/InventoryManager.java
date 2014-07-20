package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <pre>Inventory Manager class</pre>
 * <p/>
 * Provides static methods to manage Inventories
 *
 * @author slipcor
 * @version v0.10.2
 */

public final class InventoryManager {

    private static final Debug DEBUG = new Debug(26);
    private static final String[] TOOLSUFFIXES = {"_AXE", "_PICKAXE", "_SPADE", "_HOE", "_SWORD", "BOW", "SHEARS"};

    private InventoryManager() {
    }

    /**
     * fully clear a player's inventory
     *
     * @param player the player to clear
     */
    public static void clearInventory(final Player player) {
        DEBUG.i("fully clear player inventory: " + player.getName(), player);

        player.closeInventory();

        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setBoots(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
    }

    /**
     * drop a player's inventory
     *
     * @param player the player to empty
     * @return a list of the items that could be returned
     */
    public static List<ItemStack> drop(final Player player) {
        List<ItemStack> returned = new ArrayList<ItemStack>();

        DEBUG.i("dropping player inventory: " + player.getName(), player);
        List<Material> exclude;
        List<ItemStack> keep;

        ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

        if (ap == null || ap.getArena() == null) {
            exclude = new ArrayList<Material>();
            keep = new ArrayList<ItemStack>();
        } else {
            ItemStack[] items = ap.getArena().getArenaConfig().getItems(CFG.ITEMS_EXCLUDEFROMDROPS);
            exclude = new ArrayList<Material>();
            for (ItemStack item : items) {
                if (item != null) {
                    exclude.add(item.getType());
                }
            }
            keep = Arrays.asList(ap.getArena().getArenaConfig().getItems(CFG.ITEMS_KEEPONRESPAWN));
        }

        for (ItemStack is : player.getInventory().getArmorContents()) {
            if ((is == null) || (is.getType().equals(Material.AIR))) {
                continue;
            }
            for (ItemStack keepItem : keep) {
                if (keepItem.getType().equals(is.getType())) {
                    if (keepItem.hasItemMeta() && keepItem.getItemMeta().hasLore()) {
                        // has lore!
                        if (is.hasItemMeta() && is.getItemMeta().hasLore() && is.getItemMeta().getLore().equals(keepItem.getItemMeta().getLore())) {
                            returned.add(is.clone());
                        }
                    } else if (keepItem.hasItemMeta() && keepItem.getItemMeta().hasDisplayName()) {
                        // has displayname!
                        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equals(keepItem.getItemMeta().getDisplayName())) {
                            returned.add(is.clone());
                        }
                    } else {
                        // has neither!
                        returned.add(is.clone());
                    }
                }
            }
            if (exclude.contains(is.getType())) {
                continue;
            }
            player.getWorld().dropItemNaturally(player.getLocation(), is);
        }
        for (ItemStack is : player.getInventory().getContents()) {
            if ((is == null) || (is.getType().equals(Material.AIR))) {
                continue;
            }
            for (ItemStack item : keep) {
                if (item.getType() != is.getType()) {
                    continue;
                }
                if (item.hasItemMeta() && !item.getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName())) {
                    continue;
                }
                if (item.hasItemMeta() && item.getItemMeta().hasLore() && !item.getItemMeta().getLore().equals(is.getItemMeta().getLore())) {
                    continue;
                }
                returned.add(is.clone());
            }
            if (exclude.contains(is.getType())) {
                continue;
            }
            player.getWorld().dropItemNaturally(player.getLocation(), is);
        }
        player.getInventory().clear();
        return returned;
    }

    public static boolean receivesDamage(final ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) {
            return false;
        }

        for (String s : TOOLSUFFIXES) {
            if (item.getType().name().endsWith(s)) {
                return true;
            }
        }

        return false;
    }

    public static void dropExp(final Player player, final int exp) {
        if (exp < 1) {
            return;
        }
        final Location loc = player.getLocation();

        try {
            Bukkit.getScheduler().runTaskLater(PVPArena.instance, new Runnable() {
                @Override
                public void run() {
                    ExperienceOrb orb = loc.getWorld().spawn(loc, ExperienceOrb.class);
                    orb.setExperience(exp);
                }
            }, 20L);
        } catch (Exception e) {

        }
    }

    public static void transferItems(Player player, Inventory blockInventory) {
        ItemStack[] oldItems = blockInventory.getContents().clone();
        for (ItemStack items : oldItems) {
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(items);
            blockInventory.remove(items);
            if (!remaining.isEmpty()) {
                for (ItemStack item : remaining.values()) {
                    blockInventory.addItem(item);
                }
            }
        }
        player.updateInventory();
    }
}
