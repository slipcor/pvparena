package net.slipcor.pvparena.commands;

import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena DISABLE Command class</pre>
 * 
 * A command to disable an arena
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Disable extends AbstractArenaCommand {
	
	public static Map<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Disable() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		
		final PAA_Stop cmd = new PAA_Stop();
		cmd.commit(arena, sender, new String[0]);
		
		arena.getArenaConfig().set(CFG.GENERAL_ENABLED, false);
		arena.getArenaConfig().save();
		arena.setLocked(true);
		
		arena.msg(sender, Language.parse(arena, MSG.ARENA_DISABLE_DONE));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.DISABLE));
	}
}
