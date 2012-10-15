package net.slipcor.pvparena.goals;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;
import net.slipcor.pvparena.runnables.TimedEndRunnable;

/**
 * <pre>Arena Goal class "Time"</pre>
 * 
 * Time is ticking ^^
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class GoalTime extends ArenaGoal {
	
	private TimedEndRunnable ter;

	public GoalTime(Arena arena) {
		super(arena, "Time");
		db = new Debug(106);
	}

	int killpriority = 1;
	
	@Override
	public String version() {
		return "v0.9.1.0";
	}

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}
	
	@Override
	public GoalTime clone() {
		return new GoalTime(arena);
	}
	
	public void commitEnd() {
		ter.commit();
	}

	@Override
	public void commitPlayerDeath(Player respawnPlayer,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		if (respawnPlayer.getKiller() == null) {
			return;
		}
		
		ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(respawnPlayer.getName()).getArenaTeam();
		
		arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY,
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				arena.parseDeathCause(respawnPlayer, event.getEntity().getLastDamageCause().getCause(), event.getEntity().getKiller())));
	
		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
				+ "spawn");	
		
		arena.unKillPlayer(respawnPlayer, event.getEntity()
				.getLastDamageCause().getCause(), respawnPlayer.getKiller());

		new InventoryRefillRunnable(arena, respawnPlayer, event.getDrops(), 0);
	}

	@Override
	public PACheck checkPlayerDeath(PACheck res, Player player) {
		if (res.getPriority() <= killpriority) {
			res.setPriority(this, killpriority);
		}
		return res;
	}
	
	@Override
	public void displayInfo(CommandSender sender) {
		
	}
	
	@Override
	public void reset(boolean force) {
		ter.commit();
		ter = null;
	}

	@Override
	public void teleportAllToSpawn() {
		int timed = arena.getArenaConfig().getInt(CFG.GOAL_TIME_END);
		if (timed > 0) {
			db.i("arena timing!");
			// initiate autosave timer
			ter = new TimedEndRunnable(arena, timed);
		}
	}
}
