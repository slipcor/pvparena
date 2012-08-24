package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			Arena.pmsg(sender, Language.parse("command.onlyplayers"));
			return;
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (!arena.hasPlayer(ap.get())) {

			arena.msg(sender, Language.parse("command.notpartofarena"));
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
