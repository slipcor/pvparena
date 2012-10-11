package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena RELOAD Command class</pre>
 * 
 * A command to reload an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_Reload extends PAA__Command {

	public PAA_Reload() {
		super(new String[0]);
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		
		String name = arena.getName();
		
		arena.stop(true);
		ArenaManager.removeArena(arena, false);
		arena = new Arena(name);
		ArenaManager.loadArena(arena.getName());
		
		arena.msg(sender, Language.parse(MSG.RELOAD_DONE));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
