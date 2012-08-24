package net.slipcor.pvparena.goals;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.runnables.InventoryRestoreRunnable;

public class GoalPlayerLives extends ArenaGoal {

	/**
	 * the first goal ever existed. player lives. That simple.
	 * 
	 * Every player has a life pool which is 
	 */
	
	public GoalPlayerLives() {
		super("PlayerLives");
	}
	HashMap<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public void commitPlayerDeath(Arena arena, Player player,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		if (!lives.containsKey(player.getName())) {
			return;
		}
		int i = lives.get(player.getName());
		db.i("lives before death: " + i);
		if (i < 1) {
			if (!arena.getArenaConfig().getBoolean("game.preventDeath")) {
				return; // stop
				//player died => commit death!
			}
			db.i("faking player death");
	
			PlayerListener.commitPlayerDeath(arena, player, event);
		} else {
			i--;
			lives.put(player.getName(), i);
			
			new InventoryRestoreRunnable(arena, player, event.getDrops(),0);
			arena.respawnPlayer(player, i, event.getEntity().getLastDamageCause().getCause(), player.getKiller());
		}
	}
}
