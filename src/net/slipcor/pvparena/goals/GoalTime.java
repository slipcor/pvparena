package net.slipcor.pvparena.goals;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;
import net.slipcor.pvparena.runnables.TimedEndRunnable;

/**
 * <pre>Arena Goal class "Time"</pre>
 * 
 * Time is ticking ^^
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class GoalTime extends ArenaGoal {
	
	private TimedEndRunnable ter;

	public GoalTime() {
		super("Time");
		db = new Debug(106);
	}

	int killpriority = 1;
	
	@Override
	public String version() {
		return "v0.10.0.0";
	}

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
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
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(respawnPlayer.getName());
		
		ArenaTeam respawnTeam = ap.getArenaTeam();
		
		arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY,
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				arena.parseDeathCause(respawnPlayer, event.getEntity().getLastDamageCause().getCause(), event.getEntity().getKiller())));
	

		SpawnManager.distribute(arena, ap);
		
		arena.unKillPlayer(respawnPlayer, event.getEntity()
				.getLastDamageCause().getCause(), respawnPlayer.getKiller());

		new InventoryRefillRunnable(arena, respawnPlayer, event.getDrops());
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
	public boolean isInternal() {
		return true;
	}

	@Override
	public void parseStart() {
		int timed = arena.getArenaConfig().getInt(CFG.GOAL_TIME_END);
		if (timed > 0) {
			db.i("arena timing!");
			// initiate autosave timer
			ter = new TimedEndRunnable(arena, timed);
		}
	}
	
	@Override
	public void reset(boolean force) {
		if (ter != null) {
			ter.commit();
			ter = null;
		}
	}
}
