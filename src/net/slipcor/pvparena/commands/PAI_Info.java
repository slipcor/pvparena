package net.slipcor.pvparena.commands;

import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
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
 * @version v0.10.0
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
		
		if (!argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		
		arena.msg(sender, Language.parse(MSG.INFO_HEAD_HEADLINE, arena.getName(), arena.getPrefix()));
		
		arena.msg(sender, Language.parse(MSG.INFO_HEAD_TEAMS, StringParser.joinSet(arena.getTeamNames(), ", ")));
		
		arena.msg(sender, StringParser.colorVar("fighting", arena.isFightInProgress()) + " | " +
				StringParser.colorVar("custom", arena.isCustomClassAlive()) + " | " +
				StringParser.colorVar("enabled", !arena.isLocked()));
		
		HashSet<String> classes = new HashSet<String>();
		for (ArenaClass ac : arena.getClasses()) {
			if (!ac.getName().equalsIgnoreCase("custom")) {
				classes.add(ac.getName());
			}
		}
		
		arena.msg(sender,  Language.parse(MSG.INFO_CLASSES, StringParser.joinSet(classes, ", ")));
		arena.msg(sender,  Language.parse(MSG.INFO_OWNER, (arena.getOwner()==null?"server":arena.getOwner())));
		
		if (arena.getRegions() != null) {
			HashSet<String> regions = new HashSet<String>();
			for (ArenaRegionShape ar : arena.getRegions()) {
				regions.add(ar.getRegionName());
			}
			
			arena.msg(sender,  Language.parse(MSG.INFO_REGIONS, StringParser.joinSet(regions, ", ")));
		}
		
		for (ArenaGoal goal : arena.getGoals()) {
			arena.msg(sender, Language.parse(MSG.INFO_GOAL_ACTIVE, goal.getName()));
			goal.displayInfo(sender);
		}
		
		for (ArenaModule mod : arena.getMods()) {
			arena.msg(sender, Language.parse(MSG.INFO_MOD_ACTIVE, mod.getName()));
			mod.displayInfo(sender);
		}
		
		for (ArenaRegionShape reg : arena.getRegions()) {
			reg.displayInfo(sender);
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.INFO));
	}
}
