package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.managers.ConfigurationManager;
import net.slipcor.pvparena.managers.TeamManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena JOIN Command class</pre>
 * 
 * A command to join an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAG_Join extends PAA__Command {

	public PAG_Join() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0,1})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}
		
		String error = ConfigurationManager.isSetup(arena);
		if (error != null) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ERROR, error));
			return;
		}
		
		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaModule commModule = null;
		
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				res = mod.checkJoin(arena, sender, res, true);
				if (res.getPriority() > priority && priority >= 0) {
					// success and higher priority
					priority = res.getPriority();
					commModule = mod;
				} else if (res.getPriority() < 0 || priority < 0) {
					// fail
					priority = res.getPriority();
					commModule = null;
				}
			}
		}
		
		if (res.hasError()) {
			arena.msg(sender, Language.parse(MSG.ERROR_ERROR, res.getError()));
			return;
		}
		
		ArenaGoal commGoal = null;
		
		for (ArenaGoal mod : PVPArena.instance.getAgm().getTypes()) {
			res = mod.checkJoin(sender, res, true);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commGoal = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commGoal = null;
			}
		}
		
		if (res.hasError()) {
			arena.msg(sender, Language.parse(MSG.ERROR_ERROR, res.getError()));
			return;
		}
		
		if (args.length < 1) {
			// usage: /pa {arenaname} join | join an arena

			args = new String[]{TeamManager.calcFreeTeam(arena)};
		}
		
		ArenaTeam team = arena.getTeam(args[0]);
		
		if (team == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, args[0]));
			return;
		}
		
		if ((commModule == null) || (commGoal == null)) {
			if (commModule != null)
				commModule.parseJoin(arena, sender, team);
			
			if (commGoal != null)
				commGoal.parseJoin(sender, team);
			return;
		}

		commModule.parseJoin(arena, sender, team);
		commGoal.parseJoin(sender, team);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
