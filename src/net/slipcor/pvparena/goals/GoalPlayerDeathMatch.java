package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;

/**
 * <pre>Arena Goal class "PlayerDeathMatch"</pre>
 * 
 * The first Arena Goal. Players have lives. When every life is lost, the player
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class GoalPlayerDeathMatch extends ArenaGoal {
	public GoalPlayerDeathMatch(Arena arena) {
		super(arena, "PlayerDeathMatch");
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
	public GoalPlayerDeathMatch clone() {
		return new GoalPlayerDeathMatch(arena);
	}

	@Override
	public void commitPlayerDeath(Player player,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		
		Player ex = player;
		
		if (player.getKiller() == null && !lives.containsKey(player.getKiller().getName())) {
			return;
		}
		player = player.getKiller();
		
		int i = lives.get(player.getName());
		db.i("kills to go: " + i);
		if (i < 1) {
			// player has won!
		} else {
			i--;
			lives.put(player.getName(), i);

			arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING_FRAGS,
					player.getName() + ChatColor.YELLOW,
					arena.parseDeathCause(ex, event.getEntity()
							.getLastDamageCause().getCause(), player),
					String.valueOf(i)));
			arena.tpPlayerToCoordName(player, "spawn");
		}
	}

	@Override
	public void configParse(YamlConfiguration config) {
		config.addDefault("game.deathmatchlives", 10);
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt("game.deathmatchlives"));
	}

	@Override
	public boolean hasSpawn(String string) {
		return (arena.isFreeForAll() && string.toLowerCase().startsWith("spawn"));
	}

	@Override
	public void teleportAllToSpawn() {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives
						.put(ap.getName(), arena.getArenaConfig().getInt("game.deathmatchlives", 3));
			}
		}
	}
}
