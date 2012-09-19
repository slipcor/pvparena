package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
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
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;
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
	
	EndRunnable er = null;

	HashMap<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public String version() {
		return "v0.9.0.0";
	}

	int priority = 1;
	
	@Override
	public PACheckResult checkEnd(PACheckResult res) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		int count = lives.size();

		if (count == 1) {
			res.setModName(getName());
			res.setPriority(priority); // yep. only one player left. go!
		} else if (count == 0) {
			res.setError(MSG.ERROR_NOPLAYERFOUND.toString());
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
	public PACheckResult checkPlayerDeath(PACheckResult res, Player player) {
		if (res.getPriority() <= priority) {
			res.setModName(getName());
			res.setPriority(priority);
		}
		return res;
	}

	@Override
	public GoalPlayerLives clone() {
		return new GoalPlayerLives(arena);
	}

	@Override
	public void commitEnd() {
		if (er != null) {
			return;
		}
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT))
					continue;
				
				PVPArena.instance.getAmm().announceWinner(arena,
						Language.parse(MSG.PLAYER_HAS_WON, ap.getName()));

				arena.broadcast(Language.parse(MSG.PLAYER_HAS_WON, ap.getName()));
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
		if (i <= 1) {
			lives.remove(player.getName());
			if (arena.getArenaConfig().getBoolean(CFG.PLAYER_PREVENTDEATH)) {
				db.i("faking player death");
				PlayerListener.finallyKillPlayer(arena, player, event);
			}
			// player died => commit death!
			PVPArena.instance.getAgm().checkAndCommit(arena);
		} else {
			i--;
			lives.put(player.getName(), i);

			new InventoryRestoreRunnable(arena, player, event.getDrops(), 0);

			ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
			
			arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING,
					respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
					arena.parseDeathCause(player, event.getEntity()
							.getLastDamageCause().getCause(), player.getKiller()),
					String.valueOf(i)));
			
			arena.tpPlayerToCoordName(player, (arena.isFreeForAll()?"":respawnTeam.getName())
					+ "spawn");
			
			arena.unKillPlayer(player, event.getEntity()
					.getLastDamageCause().getCause(), player.getKiller());
		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("lives: " + arena.getArenaConfig().getInt(CFG.GOAL_PLIVES_LIVES));
	}

	@Override
	public PACheckResult getLives(PACheckResult res, ArenaPlayer ap) {
		if (!res.hasError() && res.getPriority() <= priority) {
			res.setError("" + (lives.containsKey(ap.getName())?lives.get(ap.getName()):0));
		}
		return res;
	}

	@Override
	public boolean hasSpawn(String string) {
		return (arena.isFreeForAll() && string.toLowerCase().startsWith("spawn"));
	}

	@Override
	public void initate(Player player) {
		lives.put(player.getName(), arena.getArenaConfig().getInt(CFG.GOAL_PLIVES_LIVES));
	}
	
	@Override
	public void reset(boolean force) {
		er = null;
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

	@Override
	public void teleportAllToSpawn() {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives
						.put(ap.getName(), arena.getArenaConfig().getInt(CFG.GOAL_PLIVES_LIVES));
			}
		}
	}
}
