package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;

public class PAI_ArenaList extends PA__Command {

	public PAI_ArenaList() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!this.argCountValid(sender, args, new HashSet<Integer>(Arrays.asList(0)))) {
			return;
		}
		HashSet<String> names = new HashSet<String>();
		
		for (Arena a : Arenas.getArenas()) {
			names.add(a.getName());
		}
		
		Arena.pmsg(sender, Language.parse("arenalist.arenalist", StringParser.joinSet(names, "&r, &a")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
