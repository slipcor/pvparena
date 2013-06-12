package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena PLAYERTEAM Command class</pre>
 * 
 * A command to put a player into an arena
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_PlayerJoin extends AbstractArenaCommand {

	public PAA_PlayerJoin() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{1,2})) {
			return;
		}

		// usage: /pa {arenaname} playerjoin [spawnname] {team} | tp to a spawn
		
		Player player = Bukkit.getPlayer(args[0]);
		
		if (player == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_PLAYER_NOTFOUND, args[0]));
			return;
		}
		
		PAG_Join cmd = new PAG_Join();
		player.addAttachment(PVPArena.instance, "pvparena.join."+arena.getName(), true, 20);
		cmd.commit(arena, player, StringParser.shiftArrayBy(args, 1));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.TELEPORT));
	}
}
