package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena Command class</pre>
 * 
 * The abstract class of a general command, including perm check
 * 
 * @author slipcor
 * 
 * @version v0.9.6
 */

public abstract class PA__Command {
	public final String[] perms;
	
	public PA__Command(String[] s) {
		perms = s;
	}
	
	public boolean argCountValid(CommandSender sender, String[] args, Integer[] validCounts) {
		
		for (int i : validCounts) {
			if (i == args.length)
				return true;
		}
		
		Arena.pmsg(sender, Language.parse(MSG.ERROR_INVALID_ARGUMENT_COUNT, String.valueOf(args.length), StringParser.joinArray(validCounts, "|")));
		return false;
	}
	
	public abstract void commit(CommandSender sender, String[] args);
	public abstract String getName();
	
	public boolean hasPerms(CommandSender sender) {
		if (sender.hasPermission("pvparena.admin")) {
			return true;
		}
		
		for (String perm : perms) {
			if (sender.hasPermission(perm)) {
				return true;
			}
		}
		
		if (perms.length > 0) {
			String s[] = perms[0].split(".");

			Arena.pmsg(sender, Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.getByNode("nopermto." + s[1]))));
		} else {

			Arena.pmsg(sender, Language.parse(MSG.ERROR_NOPERM , MSG.ERROR_NOPERM_X_ADMIN.toString()));
		}
		
		return false;
	}
	
	public static PA__Command getByName(String name) {
		
		name = name.toLowerCase();
		
		if (name.contains("create") || name.equals("!c") || name.equals("new")) {
			return new PAA_Create();
		} else if (name.contains("debug") || name.equals("!d")) {
			return new PAA_Debug();
		} else if (name.contains("help") || name.equals("-h")) {
			return new PAI_Help();
		} else if (name.contains("import") || name.startsWith("!imp")) {
			return new PAA_Import();
		} else if (name.equals("install") || name.equals("!i")) {
			return new PAA_Install();
		} else if (name.contains("uninstall") || name.equals("!ui")) {
			return new PAA_Uninstall();
		} else if (name.contains("update") || name.equals("!u")) {
			return new PAA_Update();
		} else if (name.equals("list") || name.startsWith("-ls")) {
			return new PAI_ArenaList();
		} else if (name.contains("version") || name.startsWith("-v")) {
			return new PAI_Version();
		}
		
		return null;
	}

	public abstract void displayHelp(CommandSender sender);
}
