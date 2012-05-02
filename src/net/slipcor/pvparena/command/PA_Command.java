package net.slipcor.pvparena.command;

import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public abstract class PA_Command {

	protected boolean checkArgs(CommandSender sender, String[] args, int required) {
		int check = args.length;
		if (check == required) {
			return true;
		}
		Arenas.tellPlayer(sender, Language.parse("args", String.valueOf(check), String.valueOf(required)));
		return false;
	}
	
	protected boolean checkArgs(CommandSender sender, String[] args, int required1, int required2) {
		int check = args.length;
		if (check == required1 || check == required2) {
			return true;
		}
		Arenas.tellPlayer(sender, Language.parse("args", String.valueOf(check), required1 + " | " + required2));
		return false;
	}
	
	protected boolean checkArgs(CommandSender sender, String[] args, int required1, int required2, int required3) {
		int check = args.length;
		if (check == required1 || check == required2 || check == required3) {
			return true;
		}
		Arenas.tellPlayer(sender, Language.parse("args", String.valueOf(check), required1 + " | " + required2 + " | " + required3));
		return false;
	}

	public abstract void commit(CommandSender sender, String[] args);
	public abstract String getName();
	
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
		} else if (s.equals("stats")) {
			return new PAStats();
		} else if (s.equals("version")) {
			return new PAVersion();
		}
		return null;
	}
}
