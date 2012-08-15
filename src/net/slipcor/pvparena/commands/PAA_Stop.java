package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import org.bukkit.command.CommandSender;

public class PAA_Stop extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Stop() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0)))) {
			return;
		}
		
		arena.stop(true);
		arena.msg(sender, Language.parse("stop.done"));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
