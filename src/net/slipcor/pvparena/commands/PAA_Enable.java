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
 * <pre>PVP Arena ENABLE Command class</pre>
 * 
 * A command to enable an arena
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Enable extends AbstractArenaCommand {
	
	public static Map<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Enable() {
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
		
		arena.getArenaConfig().set(CFG.GENERAL_ENABLED, true);
		arena.getArenaConfig().save();
		arena.setLocked(false);
		
		arena.msg(sender, Language.parse(arena, MSG.ARENA_ENABLE_DONE));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.ENABLE));
	}
}
