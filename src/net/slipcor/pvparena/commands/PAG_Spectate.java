package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAG_Spectate extends PAA__Command {

	public PAG_Spectate() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0)))) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse("command.onlyplayers"));
			return;
		}
		

		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		// priority will be set by flags, the max priority will be called
		
		ArenaModule commit = null;
		
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				res = mod.checkJoin(arena, sender, res, false);
				if (res.getPriority() > priority && priority >= 0) {
					// success and higher priority
					priority = res.getPriority();
					commit = mod;
				} else if (res.getPriority() < 0 || priority < 0) {
					// fail
					priority = res.getPriority();
					commit = null;
				}
			}
		}
		
		if (res.hasError()) {
			arena.msg(sender, Language.parse("error.error", res.getError()));
			return;
		}
		
		if (commit == null) {
			return;
		}
		
		commit.parseSpectate(arena, (Player) sender);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
