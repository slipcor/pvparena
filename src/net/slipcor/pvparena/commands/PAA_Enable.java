package net.slipcor.pvparena.commands;

import java.util.HashMap;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena ENABLE Command class</pre>
 * 
 * A command to enable an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_Enable extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Enable() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		
		PAA_Stop cmd = new PAA_Stop();
		cmd.commit(arena, sender, new String[0]);
		
		arena.getArenaConfig().set(CFG.GENERAL_ENABLED, true);
		arena.getArenaConfig().save();
		arena.setLocked(false);
		
		arena.msg(sender, Language.parse(MSG.ARENA_ENABLE_DONE));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
