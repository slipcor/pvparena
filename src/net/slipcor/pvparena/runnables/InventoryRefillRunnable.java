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
	private final Player player;
	private final ItemStack[] items;
	private final Arena arena;
	
	public InventoryRefillRunnable(final Arena arena, final Player player, final List<ItemStack> itemList) {
		if (!arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
			this.player = player;
			this.arena = arena;
			this.items = null;
			return;
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 3L);
		this.player = player;
		this.items = new ItemStack[itemList.size()];
		this.arena = arena;
		int pos = 0;
		for (ItemStack item : itemList) {
			items[pos++] = item.clone();
		}
	}

	@Override
	public void run() {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if (aPlayer.getStatus().equals(Status.FIGHT)) {
			if ((aPlayer.getClass().equals("custom") && !arena.getArenaConfig().getBoolean(CFG.GENERAL_CUSTOMRETURNSGEAR)) || !arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
				
				
				
				ArenaClass.equip(player, items);

				if (arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)) {
					final ArenaTeam aTeam = aPlayer.getArenaTeam();
					final String color = aTeam.getColor().name();
					arena.getDebugger().i("forcing woolhead: " + aTeam.getName() + "/"
							+ color, player);
					player.getInventory().setHelmet(
							new ItemStack(Material.WOOL, 1, StringParser
									.getColorDataFromENUM(color)));
					PVPArena.instance.getAgm().refillInventory(arena, player);
				}
			} else if (aPlayer.getClass().equals("custom")) {
				InventoryManager.clearInventory(player);
				ArenaPlayer.reloadInventory(arena, player);
			} else {
				InventoryManager.clearInventory(player);
				ArenaPlayer.givePlayerFightItems(arena, player);
			}
		}
		player.setFireTicks(0);
	}
}
