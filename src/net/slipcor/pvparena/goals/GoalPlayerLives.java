package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.runnables.InventoryRestoreRunnable;

/**
 * <pre>Arena Goal class "PlayerLives"</pre>
 * 
 * The first Arena Goal. Players have lives. When every life is lost, the player
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class GoalPlayerLives extends ArenaGoal {
	public GoalPlayerLives(Arena arena) {
		super(arena, "PlayerLives");
		db = new Debug(101);
	}

	HashMap<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public String version() {
		return "v0.9.0.0";
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		if (!arena.isFreeForAll()) {
			return null; // teams are handled somewhere else
		}
		int count = 0;
		for (String s : list) {
			if (s.startsWith("spawn")) {
				count++;
			}
		}
		return count > 3 ? null : "need more spawns! ("+count+"/4)";
	}
	
	@Override
	public GoalPlayerLives clone() {
		return new GoalPlayerLives(arena);
	}

	@Override
	public void commitPlayerDeath(Player player,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		if (!lives.containsKey(player.getName())) {
			return;
		}
		int i = lives.get(player.getName());
		db.i("lives before death: " + i);
		if (i < 1) {
			if (!arena.getArenaConfig().getBoolean("game.preventDeath")) {
				return; // stop
				// player died => commit death!
			}
			db.i("faking player death");

			PlayerListener.finallyKillPlayer(arena, player, event);
		} else {
			i--;
			lives.put(player.getName(), i);

			new InventoryRestoreRunnable(arena, player, event.getDrops(), 0);
			arena.respawnPlayer(player, event.getEntity()
					.getLastDamageCause().getCause(), player.getKiller());

			ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
			
			arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING,
					respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
					arena.parseDeathCause(player, event.getEntity()
							.getLastDamageCause().getCause(), player.getKiller()),
					String.valueOf(i)));
			this.lives.put(player.getName(), i);
			arena.tpPlayerToCoordName(player, respawnTeam.getName()
					+ "spawn");
		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt("game.lives"));
	}

	@Override
	public boolean hasSpawn(String string) {
		return (arena.isFreeForAll() && string.toLowerCase().startsWith("spawn"));
	}
}
