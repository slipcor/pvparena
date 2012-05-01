package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.runnables.ArenaWarmupRunnable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAAJoinTeam extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Language.parse("onlyplayers");
			return;
		}
		
		Player player = (Player) sender;
		
		if (!checkJoin(arena, player)) {
			return;
		}

		if (!(arena.cfg.getBoolean("join.manual", true))) {
			Arenas.tellPlayer(player, Language.parse("notselectteam"), arena);
			return;
		}

		if (arena.cfg.getInt("ready.max") > 0
				&& arena.cfg.getInt("ready.max") <= Teams
						.countPlayersInTeams(arena)) {

			Arenas.tellPlayer(player, Language.parse("teamfull",
					Teams.getTeam(arena, args[0]).colorize()), arena);
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		
		if (arena.cfg.getInt("join.warmup")>0) {
			if (ap.getStatus().equals(Status.EMPTY)) {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PVPArena.instance, 
						new ArenaWarmupRunnable(arena, ap, args[0], false),
						20L * arena.cfg.getInt("join.warmup"));
				Arenas.tellPlayer(player, Language.parse("warmingup"));
				return;
			}
		}

		arena.prepare(player, false, false);
		arena.lives.put(player.getName(), arena.cfg.getInt("game.lives", 3));

		arena.tpPlayerToCoordName(player, args[0] + "lounge");
		ap.setStatus(Status.LOBBY);

		ArenaTeam team = Teams.getTeam(arena, args[0]);

		team.add(ap);

		Inventories.prepareInventory(arena, player);
		String coloredTeam = team.colorize();

		PVPArena.instance.getAmm().parseJoin(arena, player, coloredTeam);

		Arenas.tellPlayer(player, Language.parse("youjoined", coloredTeam),
				arena);
		arena.tellEveryoneExcept(player,
				Language.parse("playerjoined", player.getName(), coloredTeam));

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

}
