package net.slipcor.pvparena.commands;

import java.util.HashMap;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena GOAL Command class</pre>
 * 
 * A command to manage arena goals
 * 
 * @author slipcor
 * 
 * @version v0.9.4
 */

public class PAA_Goal extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Goal() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{1,2})) {
			return;
		}
		
		ArenaGoal goal = null;
		
		try {
			goal = PVPArena.instance.getAgm().getType(args[0].toLowerCase());
		} catch (Exception e) {
			// nothing
		}
		
		if (goal == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_GOAL_NOTFOUND, args[0], StringParser.joinSet(PVPArena.instance.getAgm().getAllGoalNames(), " ")));
			arena.msg(sender, Language.parse(MSG.GOAL_INSTALLING));
			return;
		}
		
		if (args.length < 2) {
			// toggle
			if (arena.goalToggle(goal)) {
				arena.msg(sender, Language.parse(MSG.GOAL_ADDED, args[0]));
			} else {
				arena.msg(sender, Language.parse(MSG.GOAL_REMOVED, args[0]));
			}
			return;
		}

		if (StringParser.positive.contains(args[1].toLowerCase())) {
			arena.goalAdd(goal);
			arena.msg(sender, Language.parse(MSG.GOAL_ADDED, args[0]));
			return;
		}
		
		if (StringParser.negative.contains(args[1].toLowerCase())) {
			arena.goalRemove(goal);
			arena.msg(sender, Language.parse(MSG.GOAL_REMOVED, args[0]));
			return;
		}
			
		// usage: /pa {arenaname} goal [goal] {value}

		arena.msg(sender, Language.parse(MSG.ERROR_INVALID_VALUE, args[1]));
		arena.msg(sender, Language.parse(MSG.ERROR_POSITIVES, StringParser.joinSet(StringParser.positive, " | ")));
		arena.msg(sender, Language.parse(MSG.ERROR_NEGATIVES, StringParser.joinSet(StringParser.negative, " | ")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.GOAL));
	}
}
