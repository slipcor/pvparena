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

public class PAA_Create extends AbstractGlobalCommand {

	public PAA_Create() {
		super(new String[] {"pvparena.create"});
	}

	@Override
	public void commit(final CommandSender sender, final String[] args) {
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
		
		Arena arena = ArenaManager.getArenaByName(args[0]);
		
		if (arena != null) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ARENA_EXISTS, arena.getName()));
			return;
		}
		
		arena = new Arena(args[0]);
		
		if (!sender.hasPermission("pvparena.admin")) {
			// no admin perms => create perms => set owner
			arena.setOwner(sender.getName());
		}
		
		if (args.length > 1) {
			// preset arena stuff based on legacy stuff
			arena.getLegacyGoals(args[1]);
		} else if (args.length == 0) {
			arena.getLegacyGoals("teams");
		}
		
		ArenaManager.loadArena(arena.getName());
		Arena.pmsg(sender, Language.parse(MSG.ARENA_CREATE_DONE, arena.getName()));
		arena = ArenaManager.getArenaByName(arena.getName());
		final PAA_ToggleMod cmd = new PAA_ToggleMod();
		cmd.commit(arena, sender, new String[]{"standardspectate"});
		cmd.commit(arena, sender, new String[]{"standardlounge"});
		cmd.commit(arena, sender, new String[]{"battlefieldjoin"});
		//cmd.commit(a, sender, new String[]{"warmupjoin"});
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.CREATE));
	}
}
