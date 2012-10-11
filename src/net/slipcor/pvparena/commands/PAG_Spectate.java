package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ConfigurationManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena SPECTATE Command class</pre>
 * 
 * A command to join an arena as spectator
 * 
 * @author slipcor
 * 
 * @version v0.9.3
 */

public class PAG_Spectate extends PAA__Command {

	public PAG_Spectate() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}
		
		String error = ConfigurationManager.isSetup(arena);
		if (error != null) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ERROR, error));
			return;
		}

		PACheck.handleSpectate(arena, sender);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
