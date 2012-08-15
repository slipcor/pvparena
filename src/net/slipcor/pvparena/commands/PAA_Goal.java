package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


import net.slipcor.pvparena.PA;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.neworder.ArenaGoalManager;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

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
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(1,2)))) {
			return;
		}
		
		ArenaGoal goal = null;
		
		try {
			goal = PVPArena.instance.getAtm().getType(args[0].toLowerCase());
		} catch (Exception e) {
			// nothing
		}
		
		if (goal == null) {
			arena.msg(sender, Language.parse("goal.goalnotfound", args[0], StringParser.joinSet(PVPArena.instance.getAtm().getAllGoalNames(), " ")));
			arena.msg(sender, Language.parse("goal.installing"));
			return;
		}
		
		if (args.length < 2) {
			// toggle
			if (arena.goalToggle(goal)) {
				arena.msg(sender, Language.parse("goal.goal_added", args[0]));
			} else {
				arena.msg(sender, Language.parse("goal.goal_removed", args[0]));
			}
			return;
		}

		if (PA.positive.contains(args[1].toLowerCase())) {
			arena.goalAdd(goal);
			arena.msg(sender, Language.parse("goal.goal_added", args[0]));
			return;
		}
		
		if (PA.negative.contains(args[1].toLowerCase())) {
			arena.goalRemove(goal);
			arena.msg(sender, Language.parse("goal.goal_removed", args[0]));
			return;
		}
			
		// usage: /pa {arenaname} goal [goal] {value}

		arena.msg(sender, Language.parse("error.valuenotfound", args[1]));
		arena.msg(sender, Language.parse("error.valuepos", StringParser.joinSet(PA.positive, " | ")));
		arena.msg(sender, Language.parse("error.valueneg", StringParser.joinSet(PA.negative, " | ")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
