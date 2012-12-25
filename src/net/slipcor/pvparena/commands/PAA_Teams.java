package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena TEAMS Command class</pre>
 * 
 * A command to manage arena teams
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Teams extends PAA__Command {

	public PAA_Teams() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{0,2,3})) {
			return;
		}

		// usage: /pa {arenaname} teams set [name] [value]
		// usage: /pa {arenaname} teams add [name] [value]
		// usage: /pa {arenaname} teams remove [name]
		// usage: /pa {arenaname} teams
		
		if (args.length == 0) {
			// show teams
			arena.msg(sender, Language.parse(MSG.TEAMS_LIST, StringParser.joinSet(arena.getTeamNamesColored(), "§f,")));
			return;
		}
		ArenaTeam team = arena.getTeam(args[1]);
		
		if (team == null && !args[0].equals("add")) {
			arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, args[1]));
			return;
		}
			
		if (args[0].equals("remove")) {
			arena.msg(sender,  Language.parse(MSG.TEAMS_REMOVE, team.getColoredName()));
			arena.getTeams().remove(team);
		} else if (args[0].equals("add")) {
			try {
				ChatColor c = ChatColor.valueOf(args[2].toUpperCase());
				ArenaTeam newTeam = new ArenaTeam(args[1], c.name());
				arena.getTeams().add(newTeam);

				arena.msg(sender,  Language.parse(MSG.TEAMS_ADD, newTeam.getColoredName()));
			} catch (Exception e) {
				arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[2], StringParser.joinArray(ChatColor.values(), ",")));
			}
		} else if (args[0].equals("set")) {
			try {
				ChatColor c = ChatColor.valueOf(args[2].toUpperCase());
				ArenaTeam newTeam = new ArenaTeam(args[1], c.name());
				arena.getTeams().remove(arena.getTeam(args[1]));
				arena.getTeams().add(newTeam);

				arena.msg(sender,  Language.parse(MSG.TEAMS_REMOVE, newTeam.getColoredName()));
			} catch (Exception e) {
				arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[2], StringParser.joinArray(ChatColor.values(), ",")));
			}
		} else {
			displayHelp(sender);
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.TEAMS));
	}
}
