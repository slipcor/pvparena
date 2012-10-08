package net.slipcor.pvparena.goals;

import org.bukkit.command.CommandSender;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaGoal;
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
