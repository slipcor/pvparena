package net.slipcor.pvparena.commands;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.ncloader.NCBLoadable;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena UPDATE Command class</pre>
 * 
 * A command to update modules
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Update extends PA__Command {

	public PAA_Update() {
		super(new String[0]);
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		
		HashSet<NCBLoadable> modules = new HashSet<NCBLoadable>();
		
		if (args.length < 1 || args[0].equals("mods")) {
			modules.addAll(PVPArena.instance.getAmm().getAllMods());
		} else if (args.length < 1 || args[0].equals("goals")) {
			modules.addAll(PVPArena.instance.getAgm().getAllGoals());
		} else if (args.length < 1 || args[0].equals("regionshapes")) {
			modules.addAll(PVPArena.instance.getArsm().getRegions());
		}
		
		if (modules.size() > 0) {
			for (NCBLoadable mod : modules) {
				if (mod.isInternal()) {
					continue;
				}
				PAA_Uninstall ui = new PAA_Uninstall();
				ui.commit(sender, new String[]{mod.getName()});
				PAA_Install i = new PAA_Install();
				i.commit(sender, new String[]{mod.getName()});
			}
			return;
		}
		
		PAA_Uninstall ui = new PAA_Uninstall();
		ui.commit(sender, args);
		PAA_Install i = new PAA_Install();
		i.commit(sender, args);
	}


	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.UPDATE));
	}
}
