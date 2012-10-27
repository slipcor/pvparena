package net.slipcor.pvparena.modules;

import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
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
 * @version v0.9.3
 */

public class StandardSpectate extends ArenaModule {

	public StandardSpectate() {
		super("StandardSpectate");
		db = new Debug(301);
	}
	
	int priority = 1;
	
	@Override
	public String version() {
		return "v0.9.5.5";
	}

	@Override
	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		return list.contains("spectator")?null:"spectator not set";
	}

	@Override
	public PACheck checkJoin(Arena arena, CommandSender sender,
			PACheck res, boolean join) {
		if (join)
			return res;
		
		if (res.getPriority() < priority) {
			res.setPriority(this, priority);
		}
		return res;
	}

	@Override
	public void commitSpectate(Arena arena, Player player) {
		arena.tpPlayerToCoordName(player, "spectator");
		arena.msg(player, Language.parse(MSG.NOTICE_WELCOME_SPECTATOR));
	}

	@Override
	public boolean hasSpawn(Arena arena, String string) {
		return string.equalsIgnoreCase("spectator");
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_STANDARDSPECTATE_ACTIVE);
	}
}