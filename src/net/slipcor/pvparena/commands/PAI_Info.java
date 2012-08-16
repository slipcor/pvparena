package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

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
		
		arena.msg(sender, Language.parse("info.head_headline", arena.getName(), arena.getPrefix()));
		
		if (arena.isFreeForAll()) {
			arena.msg(sender, Language.parse("info.head_teams", StringParser.joinSet(arena.getTeamNames(), ", ")));
		}
		
		for (ArenaGoal goal : PVPArena.instance.getAgm().getTypes()) {
			if (goal == null) {
				continue;
			}
			
			if (!arena.getGoals().contains(goal)) {
				arena.msg(sender, Language.parse("info.goal_inactive", arena.getName()));
				goal.displayInfo(arena, sender);
			} else {
				arena.msg(sender, Language.parse("info.goal_inactive", arena.getName()));
			}
		}
		
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				arena.msg(sender, Language.parse("info.mod_inactive", arena.getName()));
				mod.displayInfo(arena, sender);
			} else {
				arena.msg(sender, Language.parse("info.mod_inactive", arena.getName()));
			}
		}
		
		for (ArenaRegion reg : arena.getRegions()) {
			reg.displayInfo(sender);
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
