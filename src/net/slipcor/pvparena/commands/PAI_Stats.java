package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAI_Stats extends PAA__Command {

	public PAI_Stats() {
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
		// arena can be null (general stats!)
		
		//TODO/// LAAAAATER
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
