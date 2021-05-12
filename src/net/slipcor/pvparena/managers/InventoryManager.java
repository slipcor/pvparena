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
        final List<ItemStack> returned = new ArrayList<>();

        DEBUG.i("dropping player inventory: " + player.getName(), player);
        final List<Material> exclude;
        final List<ItemStack> keep;

        final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

        boolean keepAll = false;

        if (ap == null || ap.getArena() == null) {
            exclude = new ArrayList<>();
            keep = new ArrayList<>();
        } else {
            final ItemStack[] items = ap.getArena().getArenaConfig().getItems(CFG.ITEMS_EXCLUDEFROMDROPS);
            exclude = new ArrayList<>();
            for (final ItemStack item : items) {
                if (item != null) {
                    exclude.add(item.getType());
                }
            }
            keepAll = ap.getArena().getArenaConfig().getBoolean(CFG.ITEMS_KEEPALLONRESPAWN);
            if (keepAll) {
                keep = new ArrayList<>();
            } else {
                keep = Arrays.asList(ap.getArena().getArenaConfig().getItems(CFG.ITEMS_KEEPONRESPAWN));
            }
        }

        for (final ItemStack is : player.getInventory().getContents()) {

            if (is == null || is.getType() == Material.AIR) {
                continue;
            }
            for (final ItemStack item : keep) {
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
            if (keepAll) {
                returned.add(is.clone());
                continue;
            }
            player.getWorld().dropItemNaturally(player.getLocation(), is);
        }
        player.getInventory().clear();
        ap.setMayDropInventory(false);
        return returned;
    }

    public static boolean receivesDamage(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        for (final String s : TOOLSUFFIXES) {
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
                    final ExperienceOrb orb = loc.getWorld().spawn(loc, ExperienceOrb.class);
                    orb.setExperience(exp);
                }
            }, 20L);
        } catch (final Exception e) {

        }
    }

    public static void transferItems(final Player player, final Inventory blockInventory) {
        final ItemStack[] oldItems = blockInventory.getContents().clone();
        for (final ItemStack items : oldItems) {
            final Map<Integer, ItemStack> remaining = player.getInventory().addItem(items);
            blockInventory.remove(items);
            if (!remaining.isEmpty()) {
                for (final ItemStack item : remaining.values()) {
                    blockInventory.addItem(item);
                }
            }
        }
    }
}
