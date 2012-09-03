package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena DEBUG Command class</pre>
 * 
 * A command to toggle debugging
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_Debug extends PA__Command {

	public PAA_Debug() {
		super(new String[0]);
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!this.argCountValid(sender, args, new Integer[]{0,1})) {
			return;
		}
		
		if (args.length > 0) {
			PVPArena.instance.getConfig().set("debug", args[0]);
		}
		
		Debug.load(PVPArena.instance, sender);
		return;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
