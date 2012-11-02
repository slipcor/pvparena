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
 * @version v0.9.5
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
            /pvparena help custom | help customizing
            /pvparena help game | help ingame
            /pvparena help info | help getting information
		 */
		
		if (args.length > 0) {
			if (args[0].equals("admin")) {
				Arena.pmsg(sender, Language.parse(MSG.HELP_HEADLINE, "admin"));
				Arena.pmsg(sender, "/pa help check");
				Arena.pmsg(sender, "/pa help debug");
				Arena.pmsg(sender, "/pa help disable");
				Arena.pmsg(sender, "/pa help enable");
				Arena.pmsg(sender, "/pa help reload");
				Arena.pmsg(sender, "/pa help remove");
				Arena.pmsg(sender, "/pa help stop");
				Arena.pmsg(sender, "/pa help teleport");
				
			} else if (args[0].equals("setup")) {
				Arena.pmsg(sender, Language.parse(MSG.HELP_HEADLINE, "setup"));
				Arena.pmsg(sender, "/pa help autosetup");
				Arena.pmsg(sender, "/pa help class");
				Arena.pmsg(sender, "/pa help create");
				Arena.pmsg(sender, "/pa help gamemode");
				Arena.pmsg(sender, "/pa help goal");
				Arena.pmsg(sender, "/pa help set");
				Arena.pmsg(sender, "/pa help setowner");
				Arena.pmsg(sender, "/pa help spawn");
				
			} else if (args[0].equals("custom")) {
				Arena.pmsg(sender, Language.parse(MSG.HELP_HEADLINE, "custom"));
				Arena.pmsg(sender, "/pa help blacklist");
				Arena.pmsg(sender, "/pa help edit");
				Arena.pmsg(sender, "/pa help install");
				Arena.pmsg(sender, "/pa help protection");
				Arena.pmsg(sender, "/pa help region");
				Arena.pmsg(sender, "/pa help regionflag");
				Arena.pmsg(sender, "/pa help regions");
				Arena.pmsg(sender, "/pa help regiontype");
				Arena.pmsg(sender, "/pa help round");
				Arena.pmsg(sender, "/pa help uninstall");
				Arena.pmsg(sender, "/pa help update");
				Arena.pmsg(sender, "/pa help whitelist");
				
			} else if (args[0].equals("game")) {
				Arena.pmsg(sender, Language.parse(MSG.HELP_HEADLINE, "game"));

				Arena.pmsg(sender, "/pa help chat");
				Arena.pmsg(sender, "/pa help join");
				Arena.pmsg(sender, "/pa help leave");
				Arena.pmsg(sender, "/pa help spectate");
			} else if (args[0].equals("info")) {
				Arena.pmsg(sender, Language.parse(MSG.HELP_HEADLINE, "info"));

				Arena.pmsg(sender, "/pa help arenalist");
				Arena.pmsg(sender, "/pa help help");
				Arena.pmsg(sender, "/pa help info");
				Arena.pmsg(sender, "/pa help list");
				Arena.pmsg(sender, "/pa help ready");
				Arena.pmsg(sender, "/pa help stats");
				Arena.pmsg(sender, "/pa help version");
			} else {
				PAA__Command acmd = PAA__Command.getByName(args[0]);
				if (acmd != null) {
					acmd.displayHelp(sender);
					return;
				}
				
				if (args[0].equals("arenalist")) {
					args[0] = "list";
				}
				
				PA__Command cmd = PA__Command.getByName(args[0]);
				if (cmd != null) {
					cmd.displayHelp(sender);
					return;
				}
			}
		}
		
		Arena.pmsg(sender, Language.parse(MSG.HELP_HEADLINE, ""));
		Arena.pmsg(sender, Language.parse(MSG.HELP_ADMIN, "/pvparena help admin"));
		Arena.pmsg(sender, Language.parse(MSG.HELP_SETUP, "/pvparena help setup"));
		Arena.pmsg(sender, Language.parse(MSG.HELP_CUSTOM, "/pvparena help custom"));
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
