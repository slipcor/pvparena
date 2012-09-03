package net.slipcor.pvparena.commands;

import java.util.HashSet;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena READY Command class</pre>
 * 
 * A command to ready up inside the arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAI_Ready extends PAA__Command {

	public PAI_Ready() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0,1})) {
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (!arena.hasPlayer(ap.get())) {

			arena.msg(sender, Language.parse(MSG.ERROR_NOT_IN_ARENA));
			return;
		}
		
		if (args.length < 1) {
			
			if (ap.getStatus().equals(Status.LOUNGE)) {
				return;
			}
			
			if (ap.getArenaClass() == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_READY_NOCLASS));
				return;
			}
			ap.setStatus(Status.READY);
			arena.msg(sender, Language.parse(MSG.READY_DONE));
			
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
				arena.msg(sender, Language.parse(MSG.ERROR_ERROR, res.getError()));
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
		arena.msg(sender, Language.parse(MSG.READY_LIST, StringParser.joinSet(names, ", ")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
