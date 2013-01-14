package net.slipcor.pvparena.modules;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
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
 * <pre>
 * Arena Module class "BattlefieldJoin"
 * </pre>
 * 
 * Enables direct joining to the battlefield
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class BattlefieldJoin extends ArenaModule {

	private final static int PRIORITY = 0;

	public BattlefieldJoin() {
		super("BattlefieldJoin");
		debug = new Debug(300);
	}

	@Override
	public String version() {
		return "v0.10.2.0";
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

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());

		if (aPlayer.getArena() != null) {
			debug.i(this.getName(), sender);
			result.setError(this, Language.parse(
					MSG.ERROR_ARENA_ALREADY_PART_OF, aPlayer.getArena().getName()));
			return result;
		}

		result.setPriority(this, PRIORITY);
		return result;
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
		player.setStatus(Status.LOUNGE);
		team.add(player);
		if (arena.isFreeForAll()) {
			arena.tpPlayerToCoordName(player.get(), "spawn");
		} else {
			arena.tpPlayerToCoordName(player.get(), team.getName() + "spawn");
		}
		arena.broadcast(Language.parse(MSG.FIGHT_BEGINS));
	}

	@Override
	public boolean isInternal() {
		return true;
	}
}