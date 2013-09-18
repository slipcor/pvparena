package net.slipcor.pvparena.commands;

import java.util.HashSet;
import java.util.Set;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.managers.ArenaManager;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena VERSION Command class</pre>
 * 
 * A command to display the plugin and module versions
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAI_Version extends AbstractGlobalCommand {

	public PAI_Version() {
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
			names.add(a.getName());
		}

		Arena.pmsg(sender, "�e�n-- PVP Arena version information --");
		Arena.pmsg(sender, "�ePVP Arena version: �l" + PVPArena.instance.getDescription().getVersion());
		if (args.length < 2 || args[1].toLowerCase().startsWith("goal")) {
			Arena.pmsg(sender, "�7-----------------------------------");
			Arena.pmsg(sender, "�cArena Goals:");
			for (ArenaGoal ag : PVPArena.instance.getAgm().getAllGoals()) {
				Arena.pmsg(sender,  "�c" + ag.getName() + " - " + ag.version());
			}
		}
		if (args.length < 2 || args[1].toLowerCase().startsWith("mod")) {
			Arena.pmsg(sender, "�7-----------------------------------");
			Arena.pmsg(sender, "�aMods:");
			for (ArenaModule am : PVPArena.instance.getAmm().getAllMods()) {
				Arena.pmsg(sender,  "�a" + am.getName() + " - " + am.version());
			}
		}
		if (args.length < 2 || args[1].toLowerCase().startsWith("reg")) {
			Arena.pmsg(sender, "�7-----------------------------------");
			Arena.pmsg(sender, "�aRegionshapes:");
			for (ArenaRegionShape ars : PVPArena.instance.getArsm().getRegions()) {
				Arena.pmsg(sender,  "�a" + ars.getName() + " - " + ars.version());
			}
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.VERSION));
	}
}
