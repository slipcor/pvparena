package net.slipcor.pvparena.modules;

import java.util.Iterator;
import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>Arena Module class "StandardLounge"</pre>
 * 
 * Enables joining to lounges instead of the battlefield
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class StandardSpectate extends ArenaModule {
	
	private int priority = 2;

	public StandardSpectate() {
		super("StandardSpectate");
		db = new Debug(301);
	}
	
	@Override
	public String version() {
		return "0.9.0.0";
	}

	@Override
	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		return list.contains("spectator")?null:"spectator not set";
	}

	@Override
	public boolean hasSpawn(String string) {
		return string.equalsIgnoreCase("spectator");
	}

	@Override
	public void parseSpectate(Arena arena, Player player) {
		arena.tpPlayerToCoordName(player, "spectator");
		arena.msg(player, Language.parse(MSG.NOTICE_WELCOME_SPECTATOR));
	}
}