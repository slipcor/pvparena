package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import org.bukkit.command.CommandSender;

public class PAA_Reload extends PAA__Command {

	public PAA_Reload() {
		super(new String[0]);
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0)))) {
			return;
		}
		
		String name = arena.getName();
		
		Arenas.removeArena(arena);
		arena = new Arena(name);
		Arenas.loadArena(arena.getName());
		
		arena.msg(sender, Language.parse("reload.done"));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
