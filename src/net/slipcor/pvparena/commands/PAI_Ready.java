package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

public class PAI_Ready extends PAA__Command {

	public PAI_Ready() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0, 1)))) {
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (!arena.hasPlayer(ap.get())) {

			arena.msg(sender, Language.parse("command.notpartofarena"));
			return;
		}
		
		if (args.length < 1) {
			
			if (ap.getStatus().equals(Status.LOUNGE)) {
				return;
			}
			
			if (ap.getaClass() == null) {
				arena.msg(sender, Language.parse("command.noclass"));
				return;
			}
			ap.setStatus(Status.READY);
			arena.msg(sender, Language.parse("ready.done"));
			
			PACheckResult res = new PACheckResult();

			ArenaModule commit = null;
			int priority = 0;
			
			for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
				if (mod.isActive(arena)) {
					res = mod.checkStart(arena, ap, res);
				}
				
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
			
			if (res.hasError()) {
				arena.msg(sender, Language.parse("error.error", res.getError()));
				return;
			}
			
			if (commit == null) {
				return;
			}
			
			arena.teleportAllToSpawn();
		}
		
		HashSet<String> names = new HashSet<String>();
		
		for (ArenaPlayer player : arena.getEveryone()) {
			if (player.getStatus().equals(Status.LOUNGE)) {
				names.add("&7" + player.getName() + "&r");
			} else if (player.getStatus().equals(Status.READY)) {
				names.add("&a" + player.getName() + "&r");
			}
		}
		arena.msg(sender, Language.parse("ready.players", StringParser.joinSet(names, ", ")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
