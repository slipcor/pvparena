package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.runnables.ArenaWarmupRunnable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAAJoin extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		// just /pa or /pvparena
		if (!(sender instanceof Player)) {
			Arenas.tellPlayer(sender, Language.parse("onlyplayers"));
			return;
		}

		Player player = (Player) sender;
		
		if (!PVPArena.hasPerms(player, arena)) {
			Arenas.tellPlayer(player, Language.parse("nopermto", Language.parse("join")));
			return;
		}

		if (!checkJoin(arena, player)) {
			return;
		}
		if (!arena.cfg.getBoolean("join.random", true)) {
			Arenas.tellPlayer(player, Language.parse("selectteam"), arena);
			return;
		}

		if (Teams.calcFreeTeam(arena) == null
				|| ((arena.cfg.getInt("ready.max") > 0) && (arena.cfg
						.getInt("ready.max") <= Teams
						.countPlayersInTeams(arena)))) {

			Arenas.tellPlayer(player, Language.parse("arenafull"), arena);
			return;
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);

		if (arena.cfg.getInt("join.warmup") > 0) {
			if (ap.getStatus().equals(Status.EMPTY)) {
				Bukkit.getServer()
						.getScheduler()
						.scheduleSyncDelayedTask(
								PVPArena.instance,
								new ArenaWarmupRunnable(arena, ap, null, false),
								20L * arena.cfg.getInt("join.warmup"));
				Arenas.tellPlayer(player, Language.parse("warmingup"));
				return;
			}
		}

		arena.prepare(player, false, false);

		Teams.choosePlayerTeam(arena, player);
		Inventories.prepareInventory(arena, player);
		ap.setStatus(Status.LOBBY);

		PVPArena.instance.getAmm().parseJoin(arena, player,
				Teams.getTeam(arena, ap).colorize());
		// process auto classing
		String autoClass = arena.cfg.getString("ready.autoclass");
		if (autoClass != null && !autoClass.equals("none")) {
			if (arena.classExists(autoClass)) {
				arena.forceChooseClass(player, null, autoClass);
			} else {
				db.w("autoclass selected that does not exist: " + autoClass);
			}
		}
	}

	@Override
	public String getName() {
		return "PAAJoin";
	}
}
