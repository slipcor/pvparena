package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena REMOVE Command class</pre>
 * 
 * A command to remove an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_Remove extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Remove() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0)))) {
			return;
		}
		
		String name = arena.getName();
		
		ArenaManager.removeArena(arena);
		Arena.pmsg(sender, Language.parse(MSG.ARENA_REMOVE_DONE, name));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
