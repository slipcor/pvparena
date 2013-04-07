package net.slipcor.pvparena.modules;

import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>
 * Arena Module class "StandardLounge"
 * </pre>
 * 
 * Enables joining to lounges instead of the battlefield
 * 
 * @author slipcor
 */

public class StandardSpectate extends ArenaModule {

	public StandardSpectate() {
		super("StandardSpectate");
		debug = new Debug(301);
	}

	private static final int PRIORITY = 2;

	@Override
	public String version() {
		return "v1.0.1.59";
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		return list.contains("spectator") ? null : "spectator not set";
	}

	@Override
	public PACheck checkJoin(final CommandSender sender, final PACheck res, final boolean join) {
		if (join) {
			return res;
		}

		if (res.getPriority() < PRIORITY) {
			res.setPriority(this, PRIORITY);
		}
		return res;
	}

	@Override
	public void commitSpectate(final Player player) {

		// standard join --> lounge
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		aPlayer.setLocation(new PALocation(player.getLocation()));
		if (aPlayer.getState() == null) {
			
			final Arena arena = aPlayer.getArena();

			final PAJoinEvent event = new PAJoinEvent(arena, player, false);
			Bukkit.getPluginManager().callEvent(event);

			aPlayer.createState(player);
			ArenaPlayer.backupAndClearInventory(arena, player);
			aPlayer.dump();
			
			
			if (aPlayer.getArenaTeam() != null && aPlayer.getArenaClass() == null) {
				final String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
				if (autoClass != null && !autoClass.equals("none") && arena.getClass(autoClass) != null) {
					arena.chooseClass(player, null, autoClass);
				}
				if (autoClass == null) {
					arena.msg(player, Language.parse(MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
					return;
				}
			}
		}
		aPlayer.setArena(arena);
		aPlayer.setStatus(Status.WATCH);

		arena.tpPlayerToCoordName(player, "spectator");
		arena.msg(player, Language.parse(MSG.NOTICE_WELCOME_SPECTATOR));
	}

	@Override
	public boolean hasSpawn(final String string) {
		return string.equalsIgnoreCase("spectator");
	}

	@Override
	public boolean isInternal() {
		return true;
	}
}