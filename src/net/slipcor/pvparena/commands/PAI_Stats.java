package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena STATS Command class</pre>
 * 
 * A command to display the player statistics
 * 
 * @author slipcor
 * 
 * @version v0.9.4
 */

public class PAI_Stats extends PAA__Command {

	public PAI_Stats() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		// arena can be null (general stats!)
		
		//TODO/// LAAAAATER
		String s = "";
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.STATS));
	}
}
