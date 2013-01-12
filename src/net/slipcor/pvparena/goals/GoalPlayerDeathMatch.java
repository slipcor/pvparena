package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.PlayerState;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>Arena Goal class "PlayerDeathMatch"</pre>
 * 
 * The first Arena Goal. Players have lives. When every life is lost, the player
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class GoalPlayerDeathMatch extends ArenaGoal {
	public GoalPlayerDeathMatch() {
		super("PlayerDeathMatch");
		db = new Debug(101);
	}
	
	EndRunnable er = null;

	HashMap<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public String version() {
		return "v0.10.2.28";
	}

	int priority = 3;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}
	
	@Override
	public PACheck checkEnd(PACheck res) {
		if (res.getPriority() > priority) {
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
		if (res.getPriority() <= priority && player.getKiller() != null && arena.hasPlayer(player.getKiller())) {
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
				if (!ap.getStatus().equals(Status.FIGHT)) {
					continue;
                                }
				ArenaModuleManager.announce(arena, Language.parse(MSG.PLAYER_HAS_WON, ap.getName()), "WINNER");

				arena.broadcast(Language.parse(MSG.PLAYER_HAS_WON, ap.getName()));
			}
			if (ArenaModuleManager.commitEnd(arena, team)) {
				return;
			}
		}	
		er = new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(Player killer,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		
		Player ex = killer;
		
		if (ex.getKiller() == null || !lives.containsKey(ex.getKiller().getName())) {
			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(ex.getName()).getArenaTeam();
				arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY,
						respawnTeam.colorizePlayer(ex) + ChatColor.YELLOW,
						arena.parseDeathCause(ex, event.getEntity()
								.getLastDamageCause().getCause(), killer)));
			}

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(ex);
				event.getDrops().clear();
			}
			
			PACheck.handleRespawn(arena,  ArenaPlayer.parsePlayer(ex.getName()), event.getDrops());
			
			return;
		}
		killer = ex.getKiller();
		
		int i = lives.get(killer.getName());
		db.i("kills to go: " + i, killer);
		if (i <= 1) {
			// player has won!
			HashSet<ArenaPlayer> plrs = new HashSet<ArenaPlayer>();
			for (ArenaPlayer ap : arena.getFighters()) {
				if (ap.getName().equals(killer.getName())) {
					continue;
				}
				plrs.add(ap);
			}
			for (ArenaPlayer ap : plrs) {
				lives.remove(ap.getName());
				db.i("faking player death", ap.get());
				arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(), true, false);
				
				ap.setStatus(Status.LOST);
				ap.addLosses();
				
				PlayerState.fullReset(arena, ap.get());
				
				if (ArenaManager.checkAndCommit(arena, false)) {
					return;
                                }
			}
			
			PACheck.handleEnd(arena, false);
		} else {
			i--;
			lives.put(killer.getName(), i);
			
			ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(ex.getName()).getArenaTeam();
			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING_FRAGS,
						respawnTeam.colorizePlayer(ex) + ChatColor.YELLOW,
						arena.parseDeathCause(ex, event.getEntity()
								.getLastDamageCause().getCause(), killer),
						String.valueOf(i)));
			}

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(ex);
				event.getDrops().clear();
			}
			
			PACheck.handleRespawn(arena,  ArenaPlayer.parsePlayer(ex.getName()), event.getDrops());

		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
	}

	@Override
	public PACheck getLives(PACheck res, ArenaPlayer ap) {
		if (!res.hasError() && res.getPriority() <= priority) {
			res.setError(this, "" + (arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES)-(lives.containsKey(ap.getName())?lives.get(ap.getName()):0)));
		}
		return res;
	}

	@Override
	public boolean hasSpawn(String string) {
		return (arena.isFreeForAll() && string.toLowerCase().startsWith("spawn"));
	}

	@Override
	public void initate(Player player) {
		lives.put(player.getName(), arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
	}

	@Override
	public boolean isInternal() {
		return true;
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
						.put(ap.getName(), arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
			}
		}
	}
	
	@Override
	public void reset(boolean force) {
		er = null;
		lives.clear();
	}

	@Override
	public HashMap<String, Double> timedEnd(HashMap<String, Double> scores) {
		double score;
		
		for (ArenaPlayer ap : arena.getFighters()) {
			score = arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES) - (lives.containsKey(ap.getName())?lives.get(ap.getName()):0);
			if (scores.containsKey(ap)) {
				scores.put(ap.getName(), scores.get(ap.getName())+score);
			} else {
				scores.put(ap.getName(), score);
			}
		}
		
		return scores;
	}
	
	@Override
	public void unload(Player player) {
		lives.remove(player.getName());
		if (allowsJoinInBattle()) {
			arena.hasNotPlayed(ArenaPlayer.parsePlayer(player.getName()));
                }
	}
}
