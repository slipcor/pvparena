package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena UPDATE Command class</pre>
 * 
 * A command to update modules
 * 
 * @author slipcor
 * 
 * @version v0.9.4
 */

public class PAA_Update extends PA__Command {

	public PAA_Update() {
		super(new String[0]);
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		PAA_Uninstall ui = new PAA_Uninstall();
		ui.commit(sender, args);
		PAA_Install i = new PAA_Install();
		i.commit(sender, args);
	}


	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.UPDATE));
	}
}
