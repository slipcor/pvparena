package net.slipcor.pvparena.runnables;

import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.managers.InventoryManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * <pre>Arena Runnable class "Inventory"</pre>
 * 
 * An arena timer to restore a player's inventory
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class InventoryRefillRunnable implements Runnable {
	private Player player;
	private ItemStack[] items;
	private Arena arena;
	
	public InventoryRefillRunnable(Arena a, Player p, List<ItemStack> isi) {
		if (!a.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
			return;
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 3L);
		player = p;
		items = new ItemStack[isi.size()];
		arena = a;
		int i = 0;
		for (ItemStack item : isi) {
			items[i++] = item.clone();
		}
	}

	@Override
	public void run() {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (ap.getStatus().equals(Status.FIGHT)) {
			if (ap.getClass().equals("custom") || !arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
				ArenaClass.equip(player, items);

				if (arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)) {
					ArenaTeam aTeam = ap.getArenaTeam();
					String color = aTeam.getColor().name();
					InventoryManager.db.i("forcing woolhead: " + aTeam.getName() + "/"
							+ color, player);
					player.getInventory().setHelmet(
							new ItemStack(Material.WOOL, 1, StringParser
									.getColorDataFromENUM(color)));
				}
			} else {
				InventoryManager.clearInventory(player);
				ArenaPlayer.givePlayerFightItems(arena, player);
			}
		}
		player.setFireTicks(0);
	}
}
