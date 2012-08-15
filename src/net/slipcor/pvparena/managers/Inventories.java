package net.slipcor.pvparena.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.core.Debug;

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

	public static boolean receivesDamage(ItemStack item) {
		if (item == null || item.getType().equals(Material.AIR)) {
			return false;
		}
		
		return true; //TODO check!
	}
}
