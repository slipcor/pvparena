package net.slipcor.pvparena.commands;

import java.util.HashSet;
import java.util.Set;


import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena ARENALIST Command class</pre>
 * 
 * A command to display the available arenas
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAI_ArenaList extends AbstractGlobalCommand {

	public PAI_ArenaList() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!argCountValid(sender, args, new Integer[]{0})) {
			return;
		}
		final Set<String> names = new HashSet<String>();
		
		for (Arena a : ArenaManager.getArenas()) {
			names.add((a.isLocked()?"&c":(PAA_Edit.activeEdits.containsValue(a)?"&e":(a.isFightInProgress()?"&a":"&f"))) + a.getName() + "&r");
		}
		
		Arena.pmsg(sender, Language.parse(MSG.ARENA_LIST, StringParser.joinSet(names, ", ")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.ARENALIST));
	}
}
