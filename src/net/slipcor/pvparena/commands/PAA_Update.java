package net.slipcor.pvparena.commands;

import java.util.HashSet;
import java.util.Set;

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

public class PAA_Update extends AbstractGlobalCommand {

	public PAA_Update() {
		super(new String[0]);
	}

	@Override
	public void commit(final CommandSender sender, final String[] args) {
		
		final Set<NCBLoadable> modules = new HashSet<NCBLoadable>();
		
		if (args.length < 1 || args[0].equals("mods")) {
			modules.addAll(PVPArena.instance.getAmm().getAllMods());
		} else if (args.length < 1 || args[0].equals("goals")) {
			modules.addAll(PVPArena.instance.getAgm().getAllGoals());
		} else if (args.length < 1 || args[0].equals("regionshapes")) {
			modules.addAll(PVPArena.instance.getArsm().getRegions());
		}
		
		if (!modules.isEmpty()) {
			for (NCBLoadable mod : modules) {
				if (mod.isInternal()) {
					continue;
				}
				final PAA_Uninstall uninstall = new PAA_Uninstall();
				uninstall.commit(sender, new String[]{mod.getName()});
				final PAA_Install install = new PAA_Install();
				install.commit(sender, new String[]{mod.getName()});
			}
			return;
		}
		
		final PAA_Uninstall uninstall = new PAA_Uninstall();
		uninstall.commit(sender, args);
		final PAA_Install install = new PAA_Install();
		install.commit(sender, args);
	}


	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.UPDATE));
	}
}
