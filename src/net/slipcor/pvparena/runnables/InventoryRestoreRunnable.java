package net.slipcor.pvparena.runnables;

import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.managers.Inventories;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryRestoreRunnable implements Runnable {
	private Arena arena;
	private Player player;
	private ItemStack[] items;
	
	public InventoryRestoreRunnable(Arena a, Player p, List<ItemStack> isi) {
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
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (ap.getClass().equals("custom") || !arena.cfg.getBoolean("game.refillInventory")) {
			ArenaClass.equip(player, items);
		} else {
			Inventories.clearInventory(player);
			Inventories.givePlayerFightItems(arena, player);
		}
		player.setFireTicks(0);
	}

}
