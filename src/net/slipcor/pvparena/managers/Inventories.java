package net.slipcor.pvparena.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.runnables.InventoryGiveItemsRunnable;
import net.slipcor.pvparena.runnables.InventoryLoadRunnable;
import net.slipcor.pvparena.runnables.InventorySaveRunnable;

/**
 * inventory manager class
 * 
 * -
 * 
 * provides commands to save win/lose stats to a yml file
 * 
 * @author slipcor
 * 
 * @version v0.8.10
 * 
 */

public class Inventories {

	public static final Debug db = new Debug(30);

	/**
	 * fully clear a player's inventory
	 * 
	 * @param player
	 *            the player to clear
	 */
	public static void clearInventory(Player player) {
		db.i("fully clear player inventory: " + player.getName());

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
	 * @param player
	 *            the player to empty
	 */
	public static void drop(Player player) {
		db.i("dropping player inventory: " + player.getName());
		for (ItemStack is : player.getInventory().getArmorContents()) {
			if ((is == null) || (is.getType().equals(Material.AIR))) {
				continue;
			}
			player.getWorld().dropItemNaturally(player.getLocation(), is);
		}
		for (ItemStack is : player.getInventory().getContents()) {
			if ((is == null) || (is.getType().equals(Material.AIR))) {
				continue;
			}
			player.getWorld().dropItemNaturally(player.getLocation(), is);
		}
		player.getInventory().clear();
	}

	/**
	 * supply a player with class items and eventually wool head
	 * 
	 * @param player
	 *            the player to supply
	 */
	public static void givePlayerFightItems(Arena arena, Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new InventoryGiveItemsRunnable(arena, player), arena.cfg.getInt("delays.giveitems", 0) * 1L);
	}

	/**
	 * reload player inventories from saved variables
	 * 
	 * @param player
	 */
	public static void loadInventory(Arena arena, Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new InventoryLoadRunnable(player), arena.cfg.getInt("delays.inventorysave") * 1L);
	}

	/**
	 * prepare a player's inventory, back it up and clear it
	 * 
	 * @param player
	 *            the player to save
	 */
	public static void prepareInventory(Arena arena, Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new InventorySaveRunnable(player), arena.cfg.getInt("delays.inventoryprepare", 0) * 1L);
	}
}
