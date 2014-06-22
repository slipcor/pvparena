package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.slipcor.pvparena.core.StringParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;
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
	private static final String[] TOOLSUFFIXES = {"_AXE","_PICKAXE","_SPADE","_HOE","_SWORD","BOW","SHEARS"};
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
	 * @return 
	 */
	public static List<ItemStack> drop(final Player player) {
		List<ItemStack> returned = new ArrayList<ItemStack>();

		DEBUG.i("dropping player inventory: " + player.getName(), player);
		List<Material> mats;
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		
		if (ap == null || ap.getArena() == null) {
			mats = new ArrayList<Material>();
		} else {
			ItemStack[] items = ap.getArena().getArenaConfig().getItems(CFG.ITEMS_EXCLUDEFROMDROPS);
			mats = new ArrayList<Material>();
			for (int i=0; i<items.length; i++) {
				if (items[i] != null) {
					mats.add( items[i].getType());
				}
			}
		}
		
		for (ItemStack is : player.getInventory().getArmorContents()) {
			if ((is == null) || (is.getType().equals(Material.AIR))) {
				continue;
			}
			if (mats.contains(is.getType())) {
				returned.add(is.clone());
				continue;
			}
			player.getWorld().dropItemNaturally(player.getLocation(), is);
		}
		for (ItemStack is : player.getInventory().getContents()) {
			if ((is == null) || (is.getType().equals(Material.AIR))) {
				continue;
			}
			if (mats.contains(is.getType())) {
				returned.add(is.clone());
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
		
		class RunLater implements Runnable {

			@Override
			public void run() {
				ExperienceOrb orb = loc.getWorld().spawn(loc, ExperienceOrb.class);
				orb.setExperience(exp);
			}
			
		}
		try {
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 20L);
		} catch (Exception e) {
			
		}
	}

	public static void transferItems(Player player, Inventory blockInventory) {
		ItemStack[] oldItems = blockInventory.getContents().clone();
		for (ItemStack items : oldItems) {
			HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(items);
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
