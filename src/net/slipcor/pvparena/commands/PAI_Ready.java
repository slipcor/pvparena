package net.slipcor.pvparena.commands;

import java.util.HashSet;


import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.TeamManager;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena READY Command class</pre>
 * 
 * A command to ready up inside the arena
 * 
 * @author slipcor
 * 
 * @version v0.10.0
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
		
		if (!argCountValid(sender, arena, args, new Integer[]{0,1})) {
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (!arena.hasPlayer(ap.get())) {

			arena.msg(sender, Language.parse(MSG.ERROR_NOT_IN_ARENA));
			return;
		}
		
		if (args.length < 1) {
			
			if (!ap.getStatus().equals(Status.LOUNGE)) {
				return;
			}
			
			if (ap.getArenaClass() == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_READY_NOCLASS));
				return;
			}
			if (!ap.getStatus().equals(Status.READY)) {
				arena.msg(sender, Language.parse(MSG.READY_DONE));
				arena.broadcast(Language.parse(MSG.PLAYER_READY, ap.getArenaTeam().colorizePlayer(ap.get())));
			}
			ap.setStatus(Status.READY);
			if (TeamManager.isReady(ap.getArenaTeam())) {
				arena.broadcast(Language.parse(MSG.TEAM_READY, ap.getArenaTeam().getColoredName()));
			}
			
			PACheck.handleStart(arena, sender);
			
			return;
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

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.READY));
	}
}
