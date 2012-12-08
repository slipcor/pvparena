package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena ArenaCommand class</pre>
 * 
 * The abstract class of a command belonging to an arena, including perm check
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public abstract class PAA__Command {
	public final String[] perms;

	public PAA__Command(String[] s) {
		perms = s;
	}

	public static boolean argCountValid(CommandSender sender, Arena arena,
			String[] args, Integer[] validCounts) {
		
		for (int i : validCounts) {
			if (i == args.length)
				return true;
		}

		String msg = Language.parse(MSG.ERROR_INVALID_ARGUMENT_COUNT,
				String.valueOf(args.length),
				StringParser.joinArray(validCounts, "|"));

		if (arena == null) {
			Arena.pmsg(sender, msg);
			return false;
		}
		arena.msg(sender, msg);
		return false;
	}

	public abstract void commit(Arena arena, CommandSender sender, String[] args);

	public abstract String getName();

	public boolean hasPerms(CommandSender sender, Arena arena) {
		if (sender.hasPermission("pvparena.admin")) {
			return true;
		}

		if (arena != null && sender.hasPermission("pvparena.create")
				&& sender.getName().equals(arena.getOwner())) {
			return true;
		}

		for (String perm : perms) {
			if (sender.hasPermission(perm)) {
				return true;
			}
		}

		return false;
	}

	public static PAA__Command getByName(String name) {

		name = name.toLowerCase();

		if (name.contains("blacklist") || name.equals("!bl")) {
			return new PAA_BlackList();
		} else if (name.contains("check") || name.equals("!ch")) {
			return new PAA_Check();
		} else if (name.contains("class") || name.equals("!cl")) {
			return new PAA_Class();
		} else if (name.contains("disable") || name.equals("!dis")
				|| name.equals("!off")) {
			return new PAA_Disable();
		} else if (name.contains("edit") || name.equals("!e")) {
			return new PAA_Edit();
		} else if (name.contains("enable") || name.equals("!en")
				|| name.equals("!on")) {
			return new PAA_Enable();
		} else if (name.contains("gamemode") || name.equals("!gm")) {
			return new PAA_GameMode();
		} else if (name.contains("goal") || name.equals("!g")) {
			return new PAA_Goal();
		} else if (name.contains("protect") || name.equals("!p")) {
			return new PAA_Protection();
		} else if (name.equals("regions") || name.equals("!rs")) {
			return new PAA_Regions();
		} else if (name.equals("region") || name.equals("!r")) {
			return new PAA_Region();
		} else if (name.equals("regionflag") || name.equals("!rf")) {
			return new PAA_RegionFlag();
		} else if (name.equals("regiontype") || name.equals("!rt")) {
			return new PAA_RegionType();
		} else if (name.contains("reload") || name.equals("!rl")) {
			return new PAA_Reload();
		} else if (name.contains("remove") || name.contains("delete") || name.equals("!rem") || name.equals("!del")) {
			return new PAA_Remove();
		} else if (name.contains("round") || name.equals("!rd")) {
			return new PAA_Round();
		} else if (name.equals("set") || name.equals("!s")) {
			return new PAA_Set();
		} else if (name.equals("setowner") || name.equals("!so")) {
			return new PAA_SetOwner();
		} else if (name.equals("spawn") || name.equals("!sp")) {
			return new PAA_Spawn();
		} else if (name.equals("start") || name.equals("!go")) {
			return new PAA_Start();
		} else if (name.equals("forcestop") || name.equals("stop") || name.equals("!st") || name.equals("!fs")) {
			return new PAA_Stop();
		} else if (name.contains("teleport") || name.equals("tp") || name.equals("!t")) {
			return new PAA_Teleport();
		} else if (name.contains("togglemod") || name.equals("!tm")) {
			return new PAA_ToggleMod();
		} else if (name.contains("whitelist") || name.equals("!wl")) {
			return new PAA_WhiteList();
		} else if (name.contains("chat") || name.equals("-c")) {
			return new PAG_Chat();
		} else if (name.equals("join") || name.equals("-j")) {
			return new PAG_Join();
		} else if (name.equals("leave") || name.equals("-l")) {
			return new PAG_Leave();
		} else if (name.startsWith("spec") || name.equals("-s")) {
			return new PAG_Spectate();
		} else if (name.equals("list") || name.equals("-ls")) {
			return new PAI_List();
		} else if (name.equals("ready") || name.equals("-r")) {
			return new PAI_Ready();
		} else if (name.equals("info") || name.equals("-i")) {
			return new PAI_Info();
		}

		return null;
	}

	public abstract void displayHelp(CommandSender sender);
}
