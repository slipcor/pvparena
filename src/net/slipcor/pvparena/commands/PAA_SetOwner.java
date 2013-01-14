package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena SETOWNER Command class</pre>
 * 
 * A command to set an arena owner
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_SetOwner extends AbstractArenaCommand {

	public PAA_SetOwner() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{1})) {
			return;
		}
		
		//                                   args[0]
		// usage: /pa {arenaname} setowner [playername]
		
		arena.setOwner(args[0]);
		arena.msg(sender, Language.parse(MSG.SETOWNER_DONE, args[0], arena.getName()));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.SETOWNER));
	}
}
