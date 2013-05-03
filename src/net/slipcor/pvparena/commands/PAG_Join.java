package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
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
 * @version v0.10.2
 */

public class PAG_Join extends AbstractArenaCommand {
	
	private final Debug debug = new Debug(200);

	public PAG_Join() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
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
		
		if (!PVPArena.hasPerms(sender, arena)) {
			arena.msg(sender, Language.parse(MSG.ERROR_NOPERM_JOIN));
			return;
		}
		
		final String error = ConfigurationManager.isSetup(arena);
		if (error != null) {
			arena.msg(sender, Language.parse(MSG.ERROR_ERROR, error));
			return;
		}
		
		if (ArenaRegionShape.tooFarAway(arena, (Player) sender)) {
			arena.msg(sender, Language.parse(MSG.ERROR_JOIN_RANGE));
			return;
		}
		
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());
		
		if (aPlayer.getArena() == null) {
			if (arena.hasAlreadyPlayed(aPlayer.getName())) {
				debug.i("Join_2", sender);
				arena.msg(sender, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, arena.getName()));
			} else {
				PACheck.handleJoin(arena, sender, args);
			}
		} else {
			final Arena pArena = aPlayer.getArena();
			debug.i("Join_1", sender);
			pArena.msg(sender, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, pArena.getName()));
		}
		
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.JOIN));
	}
}
