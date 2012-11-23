package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
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
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
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
 * @version v0.9.8
 */

public class GoalTank extends ArenaGoal {
	public GoalTank(Arena arena) {
		super(arena, "Tank");
		db = new Debug(108);
	}
	static HashMap<Arena, String> tanks = new HashMap<Arena, String>();
	
	EndRunnable er = null;

	HashMap<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public String version() {
		return "v0.9.8.24";
	}

	int priority = 8;
	
	@Override
	public PACheck checkEnd(PACheck res) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		int count = lives.size();

		if (count == 1 || !ArenaPlayer.parsePlayer(tanks.get(arena)).getStatus().equals(Status.FIGHT)) {
			res.setPriority(this, priority); // yep. only one player left. go!
		} else if (count == 0) {
			res.setError(this, MSG.ERROR_NOPLAYERFOUND.toString());
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		if (!arena.isFreeForAll()) {
			return null; // teams are handled somewhere else
		}
		
		if (!list.contains("tank")) {
			return "tank";
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
	public PACheck checkStart(PACheck res) {
		if (res.getPriority() < priority) {
			res.setPriority(this, priority);
		}
		return res;
	}

	@Override
	public GoalTank clone() {
		return new GoalTank(arena);
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
				
				if (tanks.containsValue(ap.getName())) {
					PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.GOAL_TANK_TANKWON), "WINNER");

					arena.broadcast(Language.parse(MSG.GOAL_TANK_TANKWON));
				} else {
					//String tank = tanks.get(arena);
					PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.GOAL_TANK_TANKDOWN), "LOSER");
					
					arena.broadcast(Language.parse(MSG.GOAL_TANK_TANKDOWN));
				}
			}
			if (PVPArena.instance.getAmm().commitEnd(arena, team)) {
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
		if (i <= 1 || tanks.get(arena).equals(player.getName())) {
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
			
			arena.tpPlayerToCoordName(player, (arena.isFreeForAll()?"":respawnTeam.getName())
					+ "spawn");
			
			arena.unKillPlayer(player, event.getEntity()
					.getLastDamageCause().getCause(), player.getKiller());
		}
	}
	
	@Override
	public void commitStart() {
		for (ArenaTeam team : arena.getTeams()) {
			SpawnManager.distribute(arena, team.getTeamMembers());
		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt(CFG.GOAL_TANK_LIVES));
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
		return (arena.isFreeForAll() && string.toLowerCase().startsWith("spawn")) || string.equals("tank");
	}

	@Override
	public void initate(Player player) {
		lives.put(player.getName(), arena.getArenaConfig().getInt(CFG.GOAL_TANK_LIVES));
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
		ArenaPlayer tank = null;
		for (ArenaTeam team : arena.getTeams()) {
			int i = (new Random()).nextInt(team.getTeamMembers().size());
			db.i("team " + team.getName() + " random " + i);
			for (ArenaPlayer ap : team.getTeamMembers()) {
				db.i("#" + i + ": " + ap.toString());
				if (i-- == 0) {
					tank = ap;
				}
				this.lives
						.put(ap.getName(), arena.getArenaConfig().getInt(CFG.GOAL_TANK_LIVES));
			}
		}
		ArenaTeam tankTeam = new ArenaTeam("tank","PINK");
		for (ArenaTeam team : arena.getTeams()) {
			team.remove(tank);
		}
		tankTeam.add(tank);
		tanks.put(arena, tank.getName());
		arena.broadcast(Language.parse(MSG.GOAL_TANK_TANKMODE, tank.getName()));
		arena.tpPlayerToCoordName(tank.get(), "tank");
		arena.getTeams().add(tankTeam);
	}
	
	@Override
	public void reset(boolean force) {
		er = null;
		lives.clear();
		tanks.remove(arena);
		arena.getTeams().remove(arena.getTeam("tank"));
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
