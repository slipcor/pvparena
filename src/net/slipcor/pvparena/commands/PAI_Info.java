package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.neworder.ArenaRegionShape;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena INFO Command class</pre>
 * 
 * A command to display the active modules of an arena and settings
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAI_Info extends PAA__Command {

	public PAI_Info() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0)))) {
			return;
		}
		
		arena.msg(sender, Language.parse(MSG.INFO_HEAD_HEADLINE, arena.getName(), arena.getPrefix()));
		
		if (arena.isFreeForAll()) {
			arena.msg(sender, Language.parse(MSG.INFO_HEAD_TEAMS, StringParser.joinSet(arena.getTeamNames(), ", ")));
		}
		
		for (ArenaGoal goal : PVPArena.instance.getAgm().getTypes()) {
			if (goal == null) {
				continue;
			}
			
			if (!arena.getGoals().contains(goal)) {
				arena.msg(sender, Language.parse(MSG.INFO_GOAL_ACTIVE, arena.getName()));
				goal.displayInfo(arena, sender);
			} else {
				arena.msg(sender, Language.parse(MSG.INFO_GOAL_INACTIVE, arena.getName()));
			}
		}
		
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				arena.msg(sender, Language.parse(MSG.INFO_MOD_ACTIVE, arena.getName()));
				mod.displayInfo(arena, sender);
			} else {
				arena.msg(sender, Language.parse(MSG.INFO_MOD_INACTIVE, arena.getName()));
			}
		}
		
		for (ArenaRegionShape reg : arena.getRegions()) {
			reg.displayInfo(sender);
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
