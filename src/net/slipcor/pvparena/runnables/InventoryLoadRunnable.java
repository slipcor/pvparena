package net.slipcor.pvparena.runnables;

import org.bukkit.entity.Player;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;

public class InventoryLoadRunnable implements Runnable {
	private Player player;
	Debug db = new Debug(68);
	public InventoryLoadRunnable(Player player) {
		this.player = player;
	}
	@Override
	public void run() {
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

}
