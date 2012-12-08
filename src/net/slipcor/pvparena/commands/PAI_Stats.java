package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena STATS Command class</pre>
 * 
 * A command to display the player statistics
 * 
 * @author slipcor
 * 
 * @version v0.10.0
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
		
		if (!argCountValid(sender, arena, args, new Integer[]{1,2})) {
			return;
		}
		
		StatisticsManager.type t = StatisticsManager.type.getByString(args[0]);
		
		if (t == null) {
			Arena.pmsg(sender, Language.parse(MSG.STATS_TYPENOTFOUND, StringParser.joinArray(type.values(), ", ")));
		}

		String[] values = StatisticsManager.read(StatisticsManager.getStats(arena, t), t, arena==null);
		String[] names = StatisticsManager.read(StatisticsManager.getStats(arena, type.NULL), t, arena==null);
		
		int max = 10;
		
		if (args.length > 1) {
			try {
				max = Integer.parseInt(args[1]);
			} catch (Exception e) {
				//
			}
		}
		
		Arena.pmsg(sender, Language.parse(MSG.STATS_HEAD, String.valueOf(max), Language.parse(MSG.getByName("STATTYPE_" + t.getName()))));
		
		for (int i = 0; i < max; i++) {
			Arena.pmsg(sender, names[i] + ": " + values[i]);
		}
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
