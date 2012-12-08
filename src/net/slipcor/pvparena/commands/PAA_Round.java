package net.slipcor.pvparena.commands;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PARound;
import net.slipcor.pvparena.classes.PARoundMap;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena ROUND Command class</pre>
 * 
 * A command to manage arena rounds
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Round extends PAA__Command {
	
	public PAA_Round() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		// /pa [arenaname] round - list rounds
		// /pa [arenaname] round [number] - list round goals
		// /pa [arenaname] round [number] [goal] - toggle round goal
		
		if (!argCountValid(sender, arena, args, new Integer[]{0,1,2})) {
			return;
		}
		
		if (args.length < 1) {
			if (arena.getRoundCount() < 1) {
				arena.msg(sender, Language.parse(MSG.ROUND_DISPLAY, "1", StringParser.joinSet(arena.getGoals(), ", ")));
			} else {
				PARoundMap rm = arena.getRounds();
				for (int i = 0; i < rm.getCount(); i ++) {
					arena.msg(sender, Language.parse(MSG.ROUND_DISPLAY, String.valueOf(i+1), StringParser.joinSet(rm.getGoals(i),", ")));
				}
			}
			return;
		}
		
		try {
			int i = Integer.parseInt(args[1]);
			PARoundMap rm = arena.getRounds();
			
			if (i >= arena.getRoundCount()) {
				i = arena.getRoundCount();
				
				rm.set(i, new PARound(new HashSet<ArenaGoal>()));
			} else if (args.length < 3) {
				arena.msg(sender, Language.parse(MSG.ROUND_DISPLAY, args[1], StringParser.joinSet(rm.getGoals(i),", ")));
				return;
			}
			
			ArenaGoal goal = null;
			
			try {
				goal = PVPArena.instance.getAgm().getGoalByName(args[2].toLowerCase());
			} catch (Exception e) {
				// nothing
			}
			
			if (goal == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_GOAL_NOTFOUND, args[2], StringParser.joinSet(PVPArena.instance.getAgm().getAllGoalNames(), " ")));
				arena.msg(sender, Language.parse(MSG.GOAL_INSTALLING));
				return;
			}

			PARound r = rm.getRound(i);
			
			if (r.toggle(arena, goal)) {
				// added
				arena.msg(sender, Language.parse(MSG.ROUND_ADDED, goal.getName()));
			} else {
				// removed
				arena.msg(sender, Language.parse(MSG.ROUND_REMOVED, goal.getName()));
			}
			
			rm.set(i, r);
			//TODO LATER
			
		} catch (Exception e) {
			arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[1]));
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.ROUND));
	}
}
