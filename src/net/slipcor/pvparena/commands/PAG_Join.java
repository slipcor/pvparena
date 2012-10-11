package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ConfigurationManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena JOIN Command class</pre>
 * 
 * A command to join an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.3
 */

public class PAG_Join extends PAA__Command {

	public PAG_Join() {
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
		
		String error = ConfigurationManager.isSetup(arena);
		if (error != null) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ERROR, error));
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (ap.getArena() != null) {
			Arena a = ap.getArena();
			a.msg(sender, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, a.getName()));
			return;
		}
		
		PACheck.handleJoin(arena, sender, args);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
