package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
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
 * @version v0.10.0
 */

public class PAG_Join extends PAA__Command {
	
	Debug db = new Debug(200);

	public PAG_Join() {
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
			db.i("Join_1");
			a.msg(sender, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, a.getName()));
			return;
		} else if (arena.hasAlreadyPlayed(ap.getName())) {
			db.i("Join_2");
			arena.msg(sender, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, arena.getName()));
			return;
		}
		
		PACheck.handleJoin(arena, sender, args);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.JOIN));
	}
}
