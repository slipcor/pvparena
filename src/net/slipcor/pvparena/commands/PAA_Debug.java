package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena DEBUG Command class</pre>
 * 
 * A command to toggle debugging
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Debug extends AbstractGlobalCommand {

	public PAA_Debug() {
		super(new String[0]);
	}

	@Override
	public void commit(final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!argCountValid(sender, args, new Integer[]{0,1})) {
			return;
		}
		
		if (args.length > 0) {
			PVPArena.instance.getConfig().set("debug", args[0]);
		}
		
		Debug.load(PVPArena.instance, sender);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.DEBUG));
	}
}
