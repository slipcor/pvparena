package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena VERSION Command class</pre>
 * 
 * A command to display the plugin and module versions
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAI_Version extends PA__Command {

	public PAI_Version() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!this.argCountValid(sender, args, new HashSet<Integer>(Arrays.asList(0)))) {
			return;
		}
		HashSet<String> names = new HashSet<String>();
		
		for (Arena a : ArenaManager.getArenas()) {
			names.add(a.getName());
		}

		Arena.pmsg(sender, "§e§n-- PVP Arena version information --");
		Arena.pmsg(sender, "§ePVP Arena version: §l" + PVPArena.instance.getDescription().getVersion());
		if (args.length < 2 || args[1].toLowerCase().startsWith("goal")) {
			Arena.pmsg(sender, "§7-----------------------------------");
			Arena.pmsg(sender, "§cArena Goals:");
			for (ArenaGoal ag : PVPArena.instance.getAgm().getTypes()) {
				Arena.pmsg(sender,  "§c" + ag.getName() + " - " + ag.version());
			}
		}
		if (args.length < 2 || args[1].toLowerCase().startsWith("mod")) {
			Arena.pmsg(sender, "§7-----------------------------------");
			Arena.pmsg(sender, "§aMods:");
			for (ArenaModule am : PVPArena.instance.getAmm().getModules()) {
				Arena.pmsg(sender,  "§a" + am.getName() + " - " + am.version());
			}
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
