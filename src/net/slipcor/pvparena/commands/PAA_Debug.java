package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.command.CommandSender;

public class PAA_Debug extends PA__Command {

	public PAA_Debug() {
		super(new String[0]);
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!this.argCountValid(sender, args, new HashSet<Integer>(Arrays.asList(0, 1)))) {
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
