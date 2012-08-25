package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena LEAVE Command class</pre>
 * 
 * A command to leave an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAG_Leave extends PAA__Command {

	public PAG_Leave() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0,1)))) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (!arena.hasPlayer(ap.get())) {

			arena.msg(sender, Language.parse(MSG.ERROR_NOT_IN_ARENA));
			return;
		}
		

		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				mod.parseLeave(arena, ap.get());
			}
		}
		
		arena.playerLeave(ap.get(), "exit");
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
