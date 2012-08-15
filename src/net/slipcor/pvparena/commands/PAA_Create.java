package net.slipcor.pvparena.commands;

import java.util.HashSet;


import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			Arena.pmsg(sender, Language.parse("command.onlyplayers"));
			return;
		}
		
		if (!this.argCountValid(sender, args, new HashSet<Integer>(1,2))) {
			return;
		}
		
		// usage: /pa create [arenaname] {legacy_arenatype}
		
		Arena a = Arenas.getArenaByName(args[0]);
		
		if (a != null) {
			Arena.pmsg(sender, Language.parse("create.arenaexists", a.getName()));
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
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
