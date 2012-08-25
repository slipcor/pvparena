package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena GAMEMODE Command class</pre>
 * 
 * A command to set the arena game mode
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

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
			arena.msg(sender, Language.parse(MSG.GAMEMODE_FREE));
		} else {
			arena.setFree(false);
			arena.msg(sender, Language.parse(MSG.GAMEMODE_TEAM));
		}
		
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
