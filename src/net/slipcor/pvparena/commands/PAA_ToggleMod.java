package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena ACTIVATE Command class</pre>
 * 
 * A command to activate modules
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_ToggleMod extends AbstractArenaCommand {

	public PAA_ToggleMod() {
		super(new String[0]);
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		if (!argCountValid(sender, arena, args,
				new Integer[]{1})) {
			return;
		}

		// pa [arenaname] togglemod [module]
		
		final String name = args[0].toLowerCase();
		final ArenaModule mod = PVPArena.instance.getAmm().getModByName(name);
		if (mod != null) {
			arena.msg(sender, Language.parse(arena, MSG.SET_DONE, mod.getName(), String.valueOf(mod.toggleEnabled(arena))));
			if (mod.checkForMissingBattleRegion()) {
				arena.msg(sender, Language.parse(arena, MSG.TOGGLEMOD_NOTICE));
			}
			return;
		}
		arena.msg(sender, Language.parse(arena, MSG.ERROR_UNKNOWN_MODULE, args[0]));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.TOGGLEMOD));
	}
}
