package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;

/**
 * <pre>Arena Goal class "PlayerLives"</pre>
 * 
 * The first Arena Goal. Players have lives. When every life is lost, the player
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class GoalPlayerLives extends ArenaGoal {
	public GoalPlayerLives() {
		super("PlayerLives");
		db = new Debug(102);
	}
	
	EndRunnable er = null;

	HashMap<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public String version() {
		return "v0.10.0.0";
	}

	int priority = 2;
	
	@Override
	public PACheck checkEnd(PACheck res) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		if (!arena.isFreeForAll()) {
			int count = TeamManager.countActiveTeams(arena);

			if (count <= 1) {
				res.setPriority(this, priority); // yep. only one team left. go!
			}
			return res;
		}
		
		int count = lives.size();

		if (count == 1) {
			res.setPriority(this, priority); // yep. only one player left. go!
		} else if (count == 0) {
			res.setError(this, MSG.ERROR_NOPLAYERFOUND.toString());
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		if (!arena.isFreeForAll()) {

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
		int count = 0;
		for (String s : list) {
			if (s.startsWith("spawn")) {
				count++;
			}
		}
		return count > 3 ? null : "need more spawns! ("+count+"/4)";
	}
	
	@Override
	public PACheck checkJoin(CommandSender sender, PACheck res, String[] args) {
		if (res.getPriority() >= priority) {
			return res;
		}

		int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
		int maxTeamPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXTEAMPLAYERS);
		
		if (maxPlayers > 0 && arena.getFighters().size() >= maxPlayers) {
			res.setError(this, Language.parse(MSG.ERROR_JOIN_ARENA_FULL));
			return res;
		}

		if (args == null || args.length < 1) {
			return res;
		}

		if (!arena.isFreeForAll()) {
			ArenaTeam team = arena.getTeam(args[0]);
			
			if (team != null) {
			
				if (maxTeamPlayers > 0 && team.getTeamMembers().size() >= maxTeamPlayers) {
					res.setError(this, Language.parse(MSG.ERROR_JOIN_TEAM_FULL));
					return res;
				}
			}
		}
		
		res.setPriority(this, priority);
		return res;
	}

	@Override
	public PACheck checkPlayerDeath(PACheck res, Player player) {
		if (res.getPriority() <= priority) {
			res.setPriority(this, priority);
		}
		return res;
	}

	@Override
	public void commitEnd(boolean force) {
		if (er != null) {
			return;
		}
		
		
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT))
					continue;

				if (arena.isFreeForAll()) {
					
					ArenaModuleManager.announce(arena, Language.parse(MSG.PLAYER_HAS_WON, ap.getName()), "WINNER");
	
					arena.broadcast(Language.parse(MSG.PLAYER_HAS_WON, ap.getName()));
				} else {
					
					ArenaModuleManager.announce(arena, Language.parse(MSG.TEAM_HAS_WON, team.getColoredName()), "WINNER");
	
					arena.broadcast(Language.parse(MSG.TEAM_HAS_WON, team.getColoredName()));
					break;
				}
			}
			
			if (ArenaModuleManager.commitEnd(arena, team)) {
				return;
			}
		}
		
		er = new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(Player player,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		if (!lives.containsKey(player.getName())) {
			return;
		}
		int i = lives.get(player.getName());
		db.i("lives before death: " + i);
		if (i <= 1) {
			lives.remove(player.getName());
			if (arena.getArenaConfig().getBoolean(CFG.PLAYER_PREVENTDEATH)) {
				db.i("faking player death");
				PlayerListener.finallyKillPlayer(arena, player, event);
			}
			// player died => commit death!
			PACheck.handleEnd(arena, false);
		} else {
			i--;
			lives.put(player.getName(), i);

			ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
			
			arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING,
					respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
					arena.parseDeathCause(player, event.getEntity()
									.getLastDamageCause().getCause(), player.getKiller()),
					String.valueOf(i)));
			
			new InventoryRefillRunnable(arena, player, event.getDrops());
			
			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(player);
				event.getDrops().clear();
			}

			SpawnManager.distribute(arena,  ArenaPlayer.parsePlayer(player.getName()));
			
			arena.unKillPlayer(player, event.getEntity()
							.getLastDamageCause().getCause(), player.getKiller());
		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt(CFG.GOAL_PLIVES_LIVES));
	}

	@Override
	public PACheck getLives(PACheck res, ArenaPlayer ap) {
		if (!res.hasError() && res.getPriority() <= priority) {
			res.setError(this, "" + (lives.containsKey(ap.getName())?lives.get(ap.getName()):0));
		}
		return res;
	}

	@Override
	public boolean hasSpawn(String string) {
		if (arena.isFreeForAll()) {
			return (string.toLowerCase().startsWith("spawn"));
		}
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().startsWith(teamName.toLowerCase()+"spawn")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void initate(Player player) {
		lives.put(player.getName(), arena.getArenaConfig().getInt(CFG.GOAL_PLIVES_LIVES));
	}
	
	@Override
	public void parseLeave(Player player) {
		if (player == null) {
			PVPArena.instance.getLogger().warning(this.getName() + ": player NULL");
			return;
		}
		if (lives.containsKey(player.getName())) {
			lives.remove(player.getName());
		}
	}

	@Override
	public void parseStart() {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives
						.put(ap.getName(), arena.getArenaConfig().getInt(CFG.GOAL_PLIVES_LIVES));
			}
		}
	}
	
	@Override
	public void reset(boolean force) {
		er = null;
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
	public void setPlayerLives(int value) {
		HashSet<String> plrs = new HashSet<String>();
		
		for (String name : lives.keySet()) {
			plrs.add(name);
		}
		
		for (String s : plrs) {
			lives.put(s, value);
		}
	}
	
	@Override
	public void setPlayerLives(ArenaPlayer ap, int value) {
		lives.put(ap.getName(), value);
	}
}
