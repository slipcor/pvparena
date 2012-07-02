package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;
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
		
		if (!PVPArena.hasPerms(player, arena)) {
			Arenas.tellPlayer(player, Language.parse("nopermto", Language.parse("join")));
			return;
		}
		
		if (!checkJoin(arena, player, true)) {
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
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
