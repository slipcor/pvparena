package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * <pre>Inventory Listener class</pre>
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class InventoryListener implements Listener {
	private Debug db = new Debug(22);

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();

		Arena arena = ArenaPlayer.parsePlayer(p.getName()).getArena();

		if (arena == null) {
			return;
		}

		db.i("InventoryClick: arena player");

		if (!BlockListener.isProtected(arena, event, "inventory")) {
			// we don't need no protection => out!
			return;
		}
		
		if (event.isShiftClick()) {
			// we never want shift clicking!
			event.setCancelled(true);
			return;
		}
		if (event.getInventory().getType()
			.equals(InventoryType.CRAFTING)) {
			// we are inside the standard 
			if (event.getRawSlot() != 5) {
				return;
			}
			event.setCancelled(arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD));
			return;
		}

		db.i("cancelling!");
		// player is carrying a flag
		event.setCancelled(true);
	}
}
