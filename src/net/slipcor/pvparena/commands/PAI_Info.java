package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
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
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		
		arena.msg(sender, Language.parse(MSG.INFO_HEAD_HEADLINE, arena.getName(), arena.getPrefix()));
		
		if (arena.isFreeForAll()) {
			arena.msg(sender, Language.parse(MSG.INFO_HEAD_TEAMS, StringParser.joinSet(arena.getTeamNames(), ", ")));
		}
		
		for (ArenaGoal goal : arena.getGoals()) {
			arena.msg(sender, Language.parse(MSG.INFO_GOAL_ACTIVE, goal.getName()));
			goal.displayInfo(sender);
		}
		
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				arena.msg(sender, Language.parse(MSG.INFO_MOD_ACTIVE, mod.getName()));
				mod.displayInfo(arena, sender);
			} else {
				arena.msg(sender, Language.parse(MSG.INFO_MOD_INACTIVE, mod.getName()));
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
