package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
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

public class PAA_Teams extends AbstractArenaCommand {

	public PAA_Teams() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{0,2,3})) {
			displayHelp(sender);
			return;
		}

		// usage: /pa {arenaname} teams set [name] [value]
		// usage: /pa {arenaname} teams add [name] [value]
		// usage: /pa {arenaname} teams remove [name]
		// usage: /pa {arenaname} teams
		
		if (args.length == 0) {
			// show teams
			arena.msg(sender, Language.parse(arena, MSG.TEAMS_LIST, StringParser.joinSet(arena.getTeamNamesColored(), ChatColor.COLOR_CHAR + "f,")));
			return;
		}
		
		if (!argCountValid(sender, arena, args, args[0].equals("remove")?new Integer[]{2}:new Integer[]{3})) {
			displayHelp(sender);
			return;
		}
		
		final ArenaTeam team = arena.getTeam(args[1]);
		
		if (team == null && !args[0].equals("add")) {
			arena.msg(sender, Language.parse(arena, MSG.ERROR_TEAMNOTFOUND, args[1]));
			return;
		}
			
		if (args[0].equals("remove")) {
			arena.msg(sender,  Language.parse(arena, MSG.TEAMS_REMOVE, team.getColoredName()));
			arena.getTeams().remove(team);
			arena.getArenaConfig().setManually("teams."+team.getName(), null);
			arena.getArenaConfig().save();
		} else if (args[0].equals("add")) {
			try {
				
				final DyeColor dColor = DyeColor.valueOf(args[2].toUpperCase());
				final ArenaTeam newTeam = new ArenaTeam(args[1], dColor.name());
				arena.getTeams().add(newTeam);
				arena.getArenaConfig().setManually("teams."+newTeam.getName(), dColor.name());
				arena.getArenaConfig().save();

				arena.msg(sender,  Language.parse(arena, MSG.TEAMS_ADD, newTeam.getColoredName()));
			} catch (Exception e) {
				arena.msg(sender, Language.parse(arena, MSG.ERROR_ARGUMENT, args[2], StringParser.joinArray(ChatColor.values(), ",")));
			}
		} else if (args[0].equals("set")) {
			try {
				final ChatColor color = ChatColor.valueOf(args[2].toUpperCase());
				final ArenaTeam newTeam = new ArenaTeam(args[1], color.name());
				arena.getTeams().remove(arena.getTeam(args[1]));
				arena.getTeams().add(newTeam);
				arena.getArenaConfig().setManually("teams."+newTeam.getName(), color.name());
				arena.getArenaConfig().save();

				arena.msg(sender,  Language.parse(arena, MSG.TEAMS_REMOVE, newTeam.getColoredName()));
			} catch (Exception e) {
				arena.msg(sender, Language.parse(arena, MSG.ERROR_ARGUMENT, args[2], StringParser.joinArray(ChatColor.values(), ",")));
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
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.TEAMS));
	}
}
