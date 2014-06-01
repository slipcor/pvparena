package net.slipcor.pvparena.runnables;

import java.util.ArrayList;
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
	private List<ItemStack> additions = new ArrayList<ItemStack>();
	private final Arena arena;
	
	public InventoryRefillRunnable(final Arena arena, final Player player, final List<ItemStack> itemList) {
		ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if (arena == null && aPlayer.getArena() == null) {
			this.player = null;
			this.items = null;
			this.arena = null;
			return;
		}
		
		if (!arena.getArenaConfig().getString(CFG.ITEMS_KEEPONRESPAWN).equals("none")) {
			ItemStack[] items = StringParser.getItemStacksFromString(arena.getArenaConfig().getString(CFG.ITEMS_KEEPONRESPAWN));
			
			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null) {
					for (ItemStack iItem : items) {
						if (iItem != null) {
							if (item.getType() != iItem.getType()) {
								continue;
							}
							
							if (item.getData().getData() != iItem.getData().getData()) {
								continue;
							}
							
							additions.add(item);
							break;
						}
					}
				}
			}
		}
		
		boolean refill = arena == null ?
				aPlayer.getArena().getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY) :
				arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY);
		
		if (!refill) {
			this.player = player;
			this.arena = arena==null?aPlayer.getArena():arena;
			this.items = null;
			return;
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 3L);
		this.player = player;
		this.items = new ItemStack[itemList.size()];
		this.arena = arena==null?aPlayer.getArena():arena;
		int pos = 0;
		for (ItemStack item : itemList) {
			items[pos++] = item.clone();
		}
	}

	@Override
	public void run() {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		arena.getDebugger().i("refilling " + player.getName());
		if (aPlayer.getStatus().equals(Status.FIGHT)) {
			if ((aPlayer.getArenaClass().getName().equals("custom") && !arena.getArenaConfig().getBoolean(CFG.GENERAL_CUSTOMRETURNSGEAR)) || !arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
				
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
			} else if (aPlayer.getArenaClass().getName().equals("custom")) {
				InventoryManager.clearInventory(player);
				ArenaPlayer.reloadInventory(arena, player);
			} else {
				InventoryManager.clearInventory(player);
				ArenaPlayer.givePlayerFightItems(arena, player);
			}
		} else {
			arena.getDebugger().i("NOT");
		}
		if (additions.size() > 0) {
			for (ItemStack item : additions) {
				player.getInventory().addItem(items);
			}
			player.updateInventory();
		}
		player.setFireTicks(0);
	}
}
