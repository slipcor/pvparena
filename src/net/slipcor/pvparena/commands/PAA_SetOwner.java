package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import org.bukkit.command.CommandSender;

public class PAA_SetOwner extends PAA__Command {

	public PAA_SetOwner() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(1)))) {
			return;
		}
		
		//                                   args[0]
		// usage: /pa {arenaname} setowner [playername]
		
		arena.setOwner(args[0]);
		arena.msg(sender, Language.parse("setowner.done", args[0], arena.getName()));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
