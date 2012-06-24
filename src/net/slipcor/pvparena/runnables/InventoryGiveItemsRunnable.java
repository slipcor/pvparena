package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Teams;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryGiveItemsRunnable implements Runnable {
	private Player player;
	private Arena arena;
	Debug db = new Debug(69);
	public InventoryGiveItemsRunnable(Arena arena, Player player) {
		this.player = player;
		this.arena = arena;
	}
	@Override
	public void run() {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);

		ArenaClass playerClass = ap.getaClass();
		if (playerClass == null) {
			return;
		}
		db.i("giving items to player '" + player.getName() + "', class '"
				+ playerClass.getName() + "'");

		playerClass.load(player);

		if (arena.cfg.getBoolean("game.woolHead", false)) {
			ArenaTeam aTeam = Teams.getTeam(arena, ap);
			String color = aTeam.getColor().name();
			db.i("forcing woolhead: " + aTeam.getName() + "/" + color);
			player.getInventory().setHelmet(
					new ItemStack(Material.WOOL, 1, StringParser
							.getColorDataFromENUM(color)));
		}
	}

}
