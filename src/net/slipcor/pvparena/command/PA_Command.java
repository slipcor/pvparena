package net.slipcor.pvparena.command;

import org.bukkit.command.CommandSender;

public abstract class PA_Command {
	public static PA_Command parseCommand(String s) {
		if (s.equals("create")) {
			return new PACreate();
		} else if (s.equals("debug")) {
			return new PADebug();
		} else if (s.equals("help")) {
			return new PAHelp();
		} else if (s.equals("list")) {
			return new PAList();
		} else if (s.equals("reload")) {
			return new PAReload();
		} else if (s.equals("version")) {
			return new PAVersion();
		}
		return null;
	}

	public abstract void commit(CommandSender sender, String[] args);
}
