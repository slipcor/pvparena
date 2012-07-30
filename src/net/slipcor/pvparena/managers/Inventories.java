package net.slipcor.pvparena.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;

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
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);

		ArenaClass playerClass = ap.getaClass();
		if (playerClass == null) {
			return;
		}
		db.i("giving items to player '" + player.getName() + "', class '"
				+ playerClass.getName() + "'");

		playerClass.load(player);

		if (arena.cfg.getBoolean("game.woolHead", false)) {
			ArenaTeam aTeam = Teams.getTeam(arena, ap);
			String color = aTeam.getColor().name();
			db.i("forcing woolhead: " + aTeam.getName() + "/" + color);
			player.getInventory().setHelmet(
					new ItemStack(Material.WOOL, 1, StringParser
							.getColorDataFromENUM(color)));
		}
	}

	/**
	 * reload player inventories from saved variables
	 * 
	 * @param player
	 */
	public static void loadInventory(Arena arena, Player player) {

		if (player == null) {
			return;
		}
		db.i("resetting inventory: " + player.getName());
		if (player.getInventory() == null) {
			return;
		}

		ArenaPlayer p = ArenaPlayer.parsePlayer(player);

		if (p.savedInventory == null) {
			return;
		}
		player.getInventory().setContents(p.savedInventory);
		player.getInventory().setArmorContents(p.savedArmor);
		p.savedInventory = null;
	}

	/**
	 * prepare a player's inventory, back it up and clear it
	 * 
	 * @param player
	 *            the player to save
	 */
	public static void prepareInventory(Arena arena, Player player) {
		db.i("saving player inventory: " + player.getName());

		ArenaPlayer p = ArenaPlayer.parsePlayer(player);
		p.savedInventory = player.getInventory().getContents().clone();
		p.savedArmor = player.getInventory().getArmorContents().clone();
		Inventories.clearInventory(player);
	}

	public static boolean receivesDamage(ItemStack item) {
		if (item == null || item.getType().equals(Material.AIR)) {
			return false;
		}
		
		return true; //TODO check!
	}
}
