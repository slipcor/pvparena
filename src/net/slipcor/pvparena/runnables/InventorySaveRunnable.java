package net.slipcor.pvparena.runnables;

import org.bukkit.entity.Player;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Inventories;

public class InventorySaveRunnable implements Runnable {
	Debug db = new Debug(67);
	Player player;
	public InventorySaveRunnable(Player player) {
		this.player = player;
	}

	@Override
	public void run() {
		db.i("saving player inventory: " + player.getName());

		ArenaPlayer p = ArenaPlayer.parsePlayer(player);
		p.savedInventory = player.getInventory().getContents().clone();
		p.savedArmor = player.getInventory().getArmorContents().clone();
		Inventories.clearInventory(player);
	}

}
