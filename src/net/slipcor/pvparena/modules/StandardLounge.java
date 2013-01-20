package net.slipcor.pvparena.modules;

import java.util.Iterator;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.PlayerStateCreateRunnable;

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
 * 
 * @version v0.10.2
 */

public class StandardLounge extends ArenaModule {

	private static final int PRIORITY = 1;

	public StandardLounge() {
		super("StandardLounge");
		debug = new Debug(300);
	}

	@Override
	public String version() {
		return "v0.10.3.0";
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		// not random! we need teams * 2 (lounge + spawn) + exit + spectator
		debug.i("parsing not random");
		final Iterator<String> iter = list.iterator();
		int lounges = 0;
		while (iter.hasNext()) {
			final String spawnName = iter.next();
			debug.i("parsing '" + spawnName + "'");
			if (arena.isFreeForAll()) {
				if (spawnName.equals("lounge")) {
					lounges++;
				}
			} else {
				if (spawnName.endsWith("lounge") && (!spawnName.equals("lounge"))) {
					lounges++;
				}
			}

		}
		if (lounges == arena.getTeams().size()) {
			return null;
		}

		return lounges + "/" + arena.getTeams().size() + "x lounge";
	}

	@Override
	public PACheck checkJoin(final CommandSender sender, final PACheck result, final boolean join) {
		if (!join) {
			return result; // we only care about joining, ignore spectators
		}
		if (result.getPriority() > PRIORITY) {
			return result; // Something already is of higher priority, ignore!
		}

		final Player player = (Player) sender;

		if (arena == null) {
			return result; // arena is null - maybe some other mod wants to
							// handle that? ignore!
		}

		if (arena.isLocked()
				&& !player.hasPermission("pvparena.admin")
				&& !(player.hasPermission("pvparena.create") && arena.getOwner()
						.equals(player.getName()))) {
			result.setError(this, Language.parse(MSG.ERROR_DISABLED));
			return result;
		}

		if (join && arena.isFightInProgress()
				&& !arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE)) {
			result.setError(this, Language.parse(MSG.ERROR_FIGHT_IN_PROGRESS));
			return result;
		}

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());

		if (aPlayer.getArena() != null) {
			debug.i(this.getName(), sender);
			result.setError(this, Language.parse(
					MSG.ERROR_ARENA_ALREADY_PART_OF, aPlayer.getArena().getName()));
			return result;
		}

		if (join && aPlayer.getArenaClass() == null) {
			final String autoClass = arena.getArenaConfig().getString(
					CFG.READY_AUTOCLASS);
			if (autoClass != null && !autoClass.equals("none") && arena.getClass(autoClass) == null) {
				result.setError(this, Language.parse(
						MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
				return result;
			}
		}

		result.setPriority(this, PRIORITY);
		return result;
	}

	@Override
	public PACheck checkStart(final ArenaPlayer player, final PACheck result) {
		if (result.getPriority() > PRIORITY) {
			return result; // Something already is of higher priority, ignore!
		}

		if (arena == null) {
			return result; // arena is null - maybe some other mod wants to
							// handle that? ignore!
		}

		final String error = String.valueOf(arena.ready());

		if (error != null) {
			result.setError(this, error);
			return result;
		}
		result.setPriority(this, PRIORITY);
		return result;
	}

	@Override
	public boolean hasSpawn(final String spawnName) {
		if (arena.isFreeForAll()) {
			return spawnName.startsWith("lounge");
		}
		for (ArenaTeam team : arena.getTeams()) {
			if (spawnName.startsWith(team.getName() + "lounge")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public void commitJoin(final Player sender, final ArenaTeam team) {

		// standard join --> lounge
		final ArenaPlayer player = ArenaPlayer.parsePlayer(sender.getName());
		Bukkit.getScheduler().runTaskLaterAsynchronously(PVPArena.instance,
				new PlayerStateCreateRunnable(player, player.get()), 2L);
		// ArenaPlayer.prepareInventory(arena, ap.get());
		player.setLocation(new PALocation(player.get().getLocation()));
		player.setArena(arena);
		team.add(player);
		if (arena.isFreeForAll()) {
			arena.tpPlayerToCoordName(player.get(), "lounge");
		} else {
			arena.tpPlayerToCoordName(player.get(), team.getName() + "lounge");
		}
		player.setStatus(Status.LOUNGE);
		arena.msg(sender, Language.parse(arena, CFG.MSG_LOUNGE));
		if (arena.isFreeForAll()) {
			arena.msg(sender,
					arena.getArenaConfig().getString(CFG.MSG_YOUJOINED));
			arena.broadcastExcept(
					sender,
					Language.parse(arena, CFG.MSG_PLAYERJOINED,
							sender.getName()));
		} else {
			arena.msg(sender,
					arena.getArenaConfig().getString(CFG.MSG_YOUJOINEDTEAM)
							.replace("%1%", team.getColoredName() + "§r"));
			arena.broadcastExcept(
					sender,
					Language.parse(arena, CFG.MSG_PLAYERJOINEDTEAM,
							sender.getName(), team.getColoredName() + "§r"));
		}
	}

	@Override
	public void parseJoin(final CommandSender player, final ArenaTeam team) {
		if (arena.startRunner != null) {
			arena.countDown();
		}
	}
}