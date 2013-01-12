package net.slipcor.pvparena.modules;

import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.PlayerStateCreateRunnable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>Arena Module class "StandardLounge"</pre>
 * 
 * Enables joining to lounges instead of the battlefield
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class StandardSpectate extends ArenaModule {

	public StandardSpectate() {
		super("StandardSpectate");
		db = new Debug(301);
	}
	
	int priority = 1;
	
	@Override
	public String version() {
		return "v0.10.0.0";
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		return list.contains("spectator")?null:"spectator not set";
	}

	@Override
	public PACheck checkJoin(CommandSender sender,
			PACheck res, boolean join) {
		if (join) {
			return res;
                }
		
		if (res.getPriority() < priority) {
			res.setPriority(this, priority);
		}
		return res;
	}

	@Override
	public void commitSpectate(Player player) {

		// standard join --> lounge
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		Bukkit.getScheduler().runTaskLaterAsynchronously(PVPArena.instance, new PlayerStateCreateRunnable(ap, ap.get()), 2L);
		//ArenaPlayer.prepareInventory(arena, ap.get());
		ap.setLocation(new PALocation(ap.get().getLocation()));
		ap.setArena(arena);
		ap.setStatus(Status.WATCH);

		arena.tpPlayerToCoordName(player, "spectator");
		arena.msg(player, Language.parse(MSG.NOTICE_WELCOME_SPECTATOR));
	}

	@Override
	public boolean hasSpawn(String string) {
		return string.equalsIgnoreCase("spectator");
	}

	@Override
	public boolean isInternal() {
		return true;
	}
}