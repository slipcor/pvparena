package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena INFO Command class</pre>
 * 
 * A command to display the active modules of an arena and settings
 * 
 * @author slipcor
 * 
 * @version v0.9.4
 */

public class PAI_Help extends PA__Command {

	public PAI_Help() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!this.argCountValid(sender, args, new Integer[]{0,1})) {
			return;
		}
		/*
            /pvparena help
            /pvparena help admin | help administrating
            /pvparena help setup | help setting up
            /pvparena help game | help ingame
            /pvparena help info | help getting information
		 */
		
		if (args.length > 0) {
			if (args[0].equals("admin")) {
				
			} else if (args[0].equals("setup")) {
				
			} else if (args[0].equals("game")) {
				
			} else if (args[0].equals("admin")) {
				
			} else {
				PA__Command cmd = PA__Command.getByName(args[0]);
				if (cmd != null) {
					cmd.displayHelp(sender);
					return;
				}
				
				PAA__Command acmd = PAA__Command.getByName(args[0]);
				if (acmd != null) {
					acmd.displayHelp(sender);
					return;
				}
			}
		}
		
		Arena.pmsg(sender, Language.parse(MSG.HELP_HEADLINE, ""));
		Arena.pmsg(sender, Language.parse(MSG.HELP_ADMIN, "/pvparena help admin"));
		Arena.pmsg(sender, Language.parse(MSG.HELP_SETUP, "/pvparena help setup"));
		Arena.pmsg(sender, Language.parse(MSG.HELP_GAME, "/pvparena help game"));
		Arena.pmsg(sender, Language.parse(MSG.HELP_INFO, "/pvparena help info"));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.HELP));
	}
}
