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
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;

/**
 * <pre>Arena Goal class "TeamDeathMatch"</pre>
 * 
 * The second Arena Goal. Arena Teams have lives. When every life is lost, the team
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.9.3
 */

public class GoalTeamDeathMatch extends ArenaGoal {
	public GoalTeamDeathMatch(Arena arena) {
		super(arena, "TeamDeathMatch");
		db = new Debug(102);
	}
	private final HashMap<String, Integer> lives = new HashMap<String, Integer>(); // flags

	@Override
	public String version() {
		return "v0.9.3.0";
	}

	int priority = 5;
	
	@Override
	public GoalTeamDeathMatch clone() {
		return new GoalTeamDeathMatch(arena);
	}

	@Override
	public PACheck checkEnd(PACheck res) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		int count = TeamManager.countActiveTeams(arena);

		if (count == 1) {
			res.setPriority(this, priority); // yep. only one team left. go!
		} else if (count == 0) {
			res.setError(this, MSG.ERROR_NOTEAMFOUND.toString());
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
	public PACheck checkPlayerDeath(PACheck res, Player player) {
		if (res.getPriority() <= priority) {
			res.setPriority(this, priority);
		}
		return res;
	}
	
	@Override
	public void commitEnd() {
		db.i("[TEAMS]");

		ArenaTeam aTeam = null;
		
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					aTeam = team;
					break;
				}
			}
		}
		
		if (aTeam != null) {
			PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.TEAM_HAS_WON,
					aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW));
		}

		if (PVPArena.instance.getAmm().commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(Player respawnPlayer,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		if (respawnPlayer.getKiller() == null) {
			return;
		}
		
		ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(respawnPlayer.getName()).getArenaTeam();
		ArenaTeam killerTeam = ArenaPlayer.parsePlayer(respawnPlayer.getKiller().getName()).getArenaTeam();
		reduceLives(arena, killerTeam);

		if (lives.get(respawnTeam.getName()) != null) {
			
			arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING_TEAM_FRAGS,
					respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
					arena.parseDeathCause(respawnPlayer, event.getEntity().getLastDamageCause().getCause(), event.getEntity().getKiller()),
					String.valueOf(arena.getArenaConfig().getInt(CFG.GOAL_TDM_LIVES)+1-lives.get(respawnTeam.getName())), respawnTeam.getColoredName()));
		
			arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
					+ "spawn");	
			
			arena.unKillPlayer(respawnPlayer, event.getEntity()
					.getLastDamageCause().getCause(), respawnPlayer.getKiller());

			new InventoryRefillRunnable(arena, respawnPlayer, event.getDrops(), 0);
		}
	}

	@Override
	public void configParse(YamlConfiguration config) {
		if (config.get("flagColors") == null) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("teams: " + StringParser.joinSet(arena.getTeamNamesColored(), "§r, "));
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt(CFG.GOAL_TDM_LIVES));
	}

	@Override
	public PACheck getLives(PACheck res, ArenaPlayer ap) {
		if (!res.hasError() && res.getPriority() <= priority) {
			res.setError(this, "" + (arena.getArenaConfig().getInt(CFG.GOAL_TDM_LIVES)-(lives.containsKey(ap.getArenaTeam().getName())?lives.get(ap.getArenaTeam().getName()):0)));
		}
		return res;
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
	public void initate(Player player) {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (lives.get(ap.getArenaTeam().getName()) == null)
			lives.put(ap.getArenaTeam().getName(), arena.getArenaConfig().getInt(CFG.GOAL_TDM_LIVES));
	}

	private void reduceLives(Arena arena, ArenaTeam team) {
		int i = this.lives.get(team.getName());
		
		if (i <= 1) {
			for (ArenaTeam otherTeam : arena.getTeams()) {
				if (otherTeam.equals(team)) {
					continue;
				}
				lives.remove(otherTeam.getName());
				for (ArenaPlayer ap : otherTeam.getTeamMembers()) {
					if (ap.getStatus().equals(Status.FIGHT)) {
						ap.setStatus(Status.LOST);
						arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(), true);
					}
				}
			}
			PACheck.handleEnd(arena);
			return;
		}
		
		lives.put(team.getName(), i-1);
	}

	@Override
	public void reset(boolean force) {
		lives.clear();
	}
	
	@Override
	public void setDefaults(YamlConfiguration config) {
		if (arena.isFreeForAll()) {
			return;
		}
		
		if (config.get("teams.free") != null) {
			config.set("teams",null);
		}
		if (config.get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			config.addDefault("teams.red",
					ChatColor.RED.name());
			config.addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
		if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)
				&& (config.get("flagColors") == null)) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	@Override
	public void teleportAllToSpawn() {
		for (ArenaTeam team : arena.getTeams()) {
				this.lives
						.put(team.getName(), arena.getArenaConfig().getInt(CFG.GOAL_TDM_LIVES));
		}
	}

	@Override
	public HashMap<String, Double> timedEnd(HashMap<String, Double> scores) {
		return scores;
	}
}
