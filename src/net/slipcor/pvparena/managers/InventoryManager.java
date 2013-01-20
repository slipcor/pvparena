package net.slipcor.pvparena.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.core.Debug;

/**
 * <pre>Inventory Manager class</pre>
 * 
 * Provides static methods to manage Inventories
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public final class InventoryManager {

	public static final Debug DEBUG = new Debug(26);
	
	private InventoryManager() {
	}

	/**
	 * fully clear a player's inventory
	 * 
	 * @param player
	 *            the player to clear
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
	 * @param player
	 *            the player to empty
	 */
	public static void drop(final Player player) {
		DEBUG.i("dropping player inventory: " + player.getName(), player);
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

	public static boolean receivesDamage(final ItemStack item) {
		if (item == null || item.getType().equals(Material.AIR)) {
			return false;
		}
		
		final String[] toolSuffixes = {"_AXE","_PICKAXE","_SPADE","_HOE","_SWORD"};
		
		for (String s : toolSuffixes) {
			if (item.getType().name().endsWith(s)) {
				return true;
			}
		}
		
		return false;
	}
}
