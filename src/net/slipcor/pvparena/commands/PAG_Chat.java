package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena CHAT Command class</pre>
 * 
 * A command to toggle global chatting
 * 
 * @author slipcor
 * 
 * @version v0.9.4
 */

public class PAG_Chat extends PAA__Command {

	public PAG_Chat() {
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
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (args.length < 1) {
			// toggle
			if (!ap.isPublicChatting()) {
				ap.setPublicChatting(true);
				if (arena.getArenaConfig().getBoolean(CFG.CHAT_ONLYPRIVATE)) {
					arena.msg(sender, Language.parse(MSG.MESSAGES_TOARENA));
				} else {
					arena.msg(sender, Language.parse(MSG.MESSAGES_TOPUBLIC));
				}
			} else {
				ap.setPublicChatting(false);
				arena.msg(sender, Language.parse(MSG.MESSAGES_TOTEAM));
			}
			return;
		}

		if (StringParser.positive.contains(args[0].toLowerCase())) {
			ap.setPublicChatting(true);
			if (arena.getArenaConfig().getBoolean(CFG.CHAT_ONLYPRIVATE)) {
				arena.msg(sender, Language.parse(MSG.MESSAGES_TOARENA));
			} else {
				arena.msg(sender, Language.parse(MSG.MESSAGES_TOPUBLIC));
			}
			return;
		}
		
		if (StringParser.negative.contains(args[0].toLowerCase())) {
			ap.setPublicChatting(false);
			arena.msg(sender, Language.parse(MSG.MESSAGES_TOTEAM));
			return;
		}
			
		// usage: /pa {arenaname} chat {value}

		arena.msg(sender, Language.parse(MSG.ERROR_INVALID_VALUE, args[0]));
		arena.msg(sender, Language.parse(MSG.ERROR_POSITIVES, StringParser.joinSet(StringParser.positive, " | ")));
		arena.msg(sender, Language.parse(MSG.ERROR_NEGATIVES, StringParser.joinSet(StringParser.negative, " | ")));
		
	}
	
	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.CHAT));
	}
}
