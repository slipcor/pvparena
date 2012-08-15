package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;

import org.bukkit.command.CommandSender;

public class PAA_GameMode extends PAA__Command {

	public PAA_GameMode() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(1)))) {
			return;
		}
		
		//                                   args[0]
		// usage: /pa {arenaname} gamemode [gamemode]
		
		// game modes: free , team
		
		if (args[0].toLowerCase().startsWith("free")) {
			arena.setFree(true);
			arena.msg(sender, Language.parse("gamemode.free"));
		} else {
			arena.setFree(false);
			arena.msg(sender, Language.parse("gamemode.team"));
		}
		
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
