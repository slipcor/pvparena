package net.slipcor.pvparena.modules;

import java.util.HashSet;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.managers.SpawnManager;

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
 */

public class BattlefieldJoin extends ArenaModule {

	private final static int PRIORITY = 1;

	public BattlefieldJoin() {
		super("BattlefieldJoin");
		debug = new Debug(300);
	}

	@Override
	public String version() {
		return "v1.0.1.59";
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
			result.setError(this, Language.parse(arena, MSG.ERROR_DISABLED));
			return result;
		}

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());

		if (aPlayer.getArena() != null) {
			aPlayer.getArena().getDebugger().i(this.getName(), sender);
			result.setError(this, Language.parse(arena,
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
		player.setLocation(new PALocation(player.get().getLocation()));

		player.setArena(arena);
		player.setStatus(Status.LOUNGE);
		team.add(player);
		final Set<PASpawn> spawns = new HashSet<PASpawn>();
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
			String arenaClass = player.getArenaClass().getName();
			spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, team.getName()+arenaClass+"spawn"));
		} else if (arena.isFreeForAll()) {
			if (team.getName().equals("free")) {
				spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, "spawn"));
			} else {
				spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, team.getName()));
			}
		} else {
			spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, team.getName()+"spawn"));
		}
		
		int pos = spawns.size(); 
		
		for (PASpawn spawn : spawns) {
			if (pos-- < 0) {
				arena.tpPlayerToCoordName(player.get(), spawn.getName());
				break;
			}
		}
		
		if (player.getState() == null) {
			
			final Arena arena = player.getArena();

			final PAJoinEvent event = new PAJoinEvent(arena, player.get(), false);
			Bukkit.getPluginManager().callEvent(event);

			player.createState(player.get());
			ArenaPlayer.backupAndClearInventory(arena, player.get());
			player.dump();
			
			
			if (player.getArenaTeam() != null && player.getArenaClass() == null) {
				final String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
				if (autoClass != null && !autoClass.equals("none") && arena.getClass(autoClass) != null) {
					arena.chooseClass(player.get(), null, autoClass);
				}
				if (autoClass == null) {
					arena.msg(player.get(), Language.parse(arena, MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
					return;
				}
			}
		} else {
			PVPArena.instance.getLogger().warning("Player has a state while joining: " + player.getName());
		}
		
		class RunLater implements Runnable {

			@Override
			public void run() {
				PACheck.handleStart(arena, sender);
			}
			
		}
		
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 10L);
	}

	@Override
	public boolean isInternal() {
		return true;
	}
}