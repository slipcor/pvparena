package net.slipcor.pvparena.commands;

import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
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
 * @version v0.10.0
 */

public class PAA_Remove extends AbstractArenaCommand {
	
	public static Map<String, Arena> activeSelections = new HashMap<String, Arena>();

	private static String removal = null;
	
	public PAA_Remove() {
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
		
		final String name = arena.getName();
		
		if (PVPArena.instance.getConfig().getBoolean("safeadmin", true)) {
			if ((removal == null) || (!removal.equals(name))) { 
				Arena.pmsg(sender, Language.parse(MSG.NOTICE_REMOVE, name));
				removal = name;
				return;
			}
			removal = null;
		}
		
		ArenaManager.removeArena(arena, true);
		Arena.pmsg(sender, Language.parse(MSG.ARENA_REMOVE_DONE, name));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.REMOVE));
	}
}
