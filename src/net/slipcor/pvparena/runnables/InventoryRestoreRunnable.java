package net.slipcor.pvparena.runnables;

import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.managers.Inventories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (ap.getStatus().equals(Status.FIGHT)) {
			if (ap.getClass().equals("custom") || !arena.getArenaConfig().getBoolean("game.refillInventory")) {
				ArenaClass.equip(player, items);
			} else {
				Inventories.clearInventory(player);
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
