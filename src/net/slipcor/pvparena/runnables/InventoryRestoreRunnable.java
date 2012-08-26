package net.slipcor.pvparena.runnables;

import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.managers.InventoryManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * <pre>Arena Runnable class "Inventory"</pre>
 * 
 * An arena timer to restore a player's inventory
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class InventoryRestoreRunnable implements Runnable {
	private Arena arena;
	private Player player;
	private ItemStack[] items;
	private int id;
	
	public InventoryRestoreRunnable(Arena a, Player p, List<ItemStack> isi, int ii) {
		id = 0;
		arena = a;
		player = p;
		items = new ItemStack[isi.size()];
		int i = 0;
		for (ItemStack item : isi) {
			items[i++] = item.clone();
		}
	}

	@Override
	public void run() {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (ap.getStatus().equals(Status.FIGHT)) {
			if (ap.getClass().equals("custom") || !arena.getArenaConfig().getBoolean("game.refillInventory")) {
				ArenaClass.equip(player, items);
			} else {
				InventoryManager.clearInventory(player);
				ArenaPlayer.givePlayerFightItems(arena, player);
			}
		}
		player.setFireTicks(0);
		Bukkit.getScheduler().cancelTask(id);
	}
	
	public void setId(int i) {
		id = i;
	}
}
