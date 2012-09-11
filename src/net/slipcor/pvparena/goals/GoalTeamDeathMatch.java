package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>Arena Goal class "TeamDeathMatch"</pre>
 * 
 * The second Arena Goal. Arena Teams have lives. When every life is lost, the team
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class GoalTeamDeathMatch extends ArenaGoal {
	public GoalTeamDeathMatch(Arena arena) {
		super(arena, "TeamDeathMatch");
		db = new Debug(102);
	}
	private final HashMap<String, Integer> lives = new HashMap<String, Integer>(); // flags

	@Override
	public String version() {
		return "v0.9.0.0";
	}
	
	@Override
	public GoalTeamDeathMatch clone() {
		return new GoalTeamDeathMatch(arena);
	}
	
	@Override
	public void setDefaults(YamlConfiguration config) {
		if (arena.isFreeForAll()) {
			return;
		}
		
		if (arena.getArenaConfig().get("teams.free") != null) {
			arena.getArenaConfig().set("teams",null);
		}
		if (arena.getArenaConfig().get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			arena.getArenaConfig().getYamlConfiguration().addDefault("teams.red",
					ChatColor.RED.name());
			arena.getArenaConfig().getYamlConfiguration().addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
		if (arena.getArenaConfig().getBoolean("game.woolFlagHead")
				&& (arena.getArenaConfig().get("flagColors") == null)) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	@Override
	public PACheckResult checkEnd(PACheckResult res) {
		int priority = 3;
		
		if (res.getPriority() > priority) {
			return res;
		}
		
		for (String teamName : lives.keySet()) {
			if (lives.get(teamName) < 1) {
				res.setPriority(priority); // yep. one has won!
			}
		}

		return res;

	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			if (!list.contains(team + "spawn")) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam) && s.endsWith("spawn")) {
						found = true;
						break;
					}
				}
				if (!found)
					return team.getName() + "spawn not set";
			}
		}
		return null;
	}
	
	@Override
	public void commitEnd() {
		db.i("[TEAMS]");

		ArenaTeam aTeam = null;
		
		for (String teamName : lives.keySet()) {
			if (lives.get(teamName) < 1) {
				aTeam = arena.getTeam(teamName);
			}
		}
		
		if (aTeam != null) {
			PVPArena.instance.getAmm().announceWinner(arena,
					Language.parse(MSG.TEAM_HAS_WON, "Team " + aTeam.getName()));

			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor()
					+ "Team " + aTeam.getName()));
		}

		if (PVPArena.instance.getAmm().commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt("goal.endtimer"));
	}

	@Override
	public void configParse(YamlConfiguration config) {
		config.addDefault("game.teamdmlives", 10);

		if (arena.getArenaConfig().get("flagColors") == null) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("teams: " + StringParser.joinSet(arena.getTeamNamesColored(), "§r, "));
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt("game.teamdmlives"));
	}

	@Override
	public String guessSpawn(String place) {
		if (!place.contains("spawn")) {
			db.i("place not found!");
			return null;
		}
		// no exact match: assume we have multiple spawnpoints
		HashMap<Integer, String> locs = new HashMap<Integer, String>();
		int i = 0;

		db.i("searching for team spawns");

		HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(i++, name);
				db.i("found match: " + name);
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		Random r = new Random();

		place = locs.get(r.nextInt(locs.size()));

		return place;
	}

	@Override
	public boolean hasSpawn(String string) {
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().startsWith(teamName.toLowerCase()+"spawn")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String ready() {
		return null;
	}

	@Override
	public void commitPlayerDeath(Player respawnPlayer,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		
		if (respawnPlayer.getKiller() == null) {
			return;
		}
		
		ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(respawnPlayer.getKiller().getName()).getArenaTeam();
		reduceLives(arena, respawnTeam);
		
		arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING_TEAM,
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				arena.parseDeathCause(respawnPlayer, event.getEntity().getLastDamageCause().getCause(), event.getEntity().getKiller()),
				String.valueOf(lives.get(respawnTeam.getName())), respawnTeam.getColoredName()));
	
		
		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
				+ "spawn");
	}
	
	private void reduceLives(Arena arena, ArenaTeam player) {
		lives.put(player.getName(), this.lives.get(player.getName())-1);
	}

	@Override
	public void reset(boolean force) {
		return;
	}

	@Override
	public void teleportAllToSpawn() {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives
						.put(ap.getName(), arena.getArenaConfig().getInt("game.teamdmlives", 10));
			}
		}
	}

	@Override
	public HashMap<String, Double> timedEnd(HashMap<String, Double> scores) {
		return scores;
	}

	@Override
	public void unload(Player player) {
	}
}
