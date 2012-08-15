package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;


import net.slipcor.pvparena.PA;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAG_Chat extends PAA__Command {
	
	private static HashSet<String> globalChatters = new HashSet<String>();

	public PAG_Chat() {
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
		
		String player = sender.getName();
		
		if (args.length < 1) {
			// toggle
			if (!globalChatters.contains(player)) {
				globalChatters.add(player);
				arena.msg(sender, Language.parse("messaging.global_on", args[1]));
			} else {
				globalChatters.remove(player);
				arena.msg(sender, Language.parse("messaging.global_off", args[1]));
			}
			return;
		}

		if (PA.positive.contains(args[0].toLowerCase())) {
			globalChatters.add(player);
			arena.msg(sender, Language.parse("messaging.global_on", args[1]));
			return;
		}
		
		if (PA.negative.contains(args[0].toLowerCase())) {
			globalChatters.remove(player);
			arena.msg(sender, Language.parse("messaging.global_off", args[1]));
			return;
		}
			
		// usage: /pa {arenaname} chat {value}

		arena.msg(sender, Language.parse("error.valuenotfound", args[0]));
		arena.msg(sender, Language.parse("error.valuepos", StringParser.joinSet(PA.positive, " | ")));
		arena.msg(sender, Language.parse("error.valueneg", StringParser.joinSet(PA.negative, " | ")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
