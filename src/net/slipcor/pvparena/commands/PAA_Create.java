package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena CREATE Command class</pre>
 * 
 * A command to create an arena
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Create extends PA__Command {

	public PAA_Create() {
		super(new String[] {"pvparena.create"});
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}
		
		if (!argCountValid(sender, args, new Integer[]{1,2})) {
			return;
		}
		
		// usage: /pa create [arenaname] {legacy_arenatype}
		
		Arena a = ArenaManager.getArenaByName(args[0]);
		
		if (a != null) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ARENA_EXISTS, a.getName()));
			return;
		}
		
		a = new Arena(args[0]);
		
		if (!sender.hasPermission("pvparena.admin")) {
			// no admin perms => create perms => set owner
			a.setOwner(sender.getName());
		}
		
		if (args.length > 1) {
			// preset arena stuff based on legacy stuff
			a.getLegacyGoals(args[1]);
		} else if (args.length == 0) {
			a.getLegacyGoals("teams");
		}
		
		ArenaManager.loadArena(a.getName());
		Arena.pmsg(sender, Language.parse(MSG.ARENA_CREATE_DONE, a.getName()));
		a = ArenaManager.getArenaByName(a.getName());
		PAA_ToggleMod cmd = new PAA_ToggleMod();
		cmd.commit(a, sender, new String[]{"standardspectate"});
		cmd.commit(a, sender, new String[]{"standardlounge"});
		cmd.commit(a, sender, new String[]{"battlefieldjoin"});
		//cmd.commit(a, sender, new String[]{"warmupjoin"});
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.CREATE));
	}
}
