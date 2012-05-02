package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Configs;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Regions;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.runnables.ArenaWarmupRunnable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAASpectate extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Arenas.tellPlayer(sender, Language.parse("onlyplayers"));
			return;
		}
		
		Player player = (Player) sender;
		
		String error = Configs.isSetup(arena);
		if (error != null) {
			Arenas.tellPlayer(player, Language.parse("arenanotsetup", error),
					arena);
			return;
		}
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(arena, ap);
		if (team != null) {
			Arenas.tellPlayer(player, Language.parse("alreadyjoined"), arena);
			return;
		}
		if (Regions.tooFarAway(arena, player)) {
			Arenas.tellPlayer(player, Language.parse("joinrange"), arena);
			return;
		}
		
		if (arena.cfg.getInt("join.warmup")>0) {
			if (ap.getStatus().equals(Status.EMPTY)) {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PVPArena.instance, 
						new ArenaWarmupRunnable(arena, ap, null, true),
						20L * arena.cfg.getInt("join.warmup"));
				Arenas.tellPlayer(player, Language.parse("warmingup"));
				return;
			}
		}
		
		arena.prepare(player, true, false);
		ap.setArena(arena);
		arena.tpPlayerToCoordName(player, "spectator");
		Inventories.prepareInventory(arena, player);
		Arenas.tellPlayer(player, Language.parse("specwelcome"), arena);
		return;
	}

	@Override
	public String getName() {
		return "PASpectate";
	}
}
