package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaRegionManager;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionShape;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PAARegion extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Arenas.tellPlayer(sender, Language.parse("onlyplayers"));
			return;
		}
		
		Player player = (Player) sender;
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("admin")));
			return;
		}
		
		if (!checkArgs(player, args, 1, 2, 3)) {
			return;
		}
		
		if (args.length == 1) {
			// pa region
			if (!Arena.regionmodify.equals("")) {
				Arenas.tellPlayer(
						player,
						Language.parse("regionalreadybeingset", Arena.regionmodify),
						arena);
				return;
			}
			Arena.regionmodify = arena.name;
			Arenas.tellPlayer(player, Language.parse("regionset"), arena);
			return;
		} else if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
			// pa region remove [regionname]
			if (arena.cfg.get("regions." + args[2]) != null) {
				arena.regions.remove(args[2]);
				arena.cfg.set("regions." + args[2], null);
				arena.cfg.save();
				Arena.regionmodify = "";
				Arenas.tellPlayer(player,
						Language.parse("regionremoved", args[2]), arena);
			} else {
				Arenas.tellPlayer(player,
						Language.parse("regionnotremoved"), arena);
			}
			return;
		}

		// pa region [regionname] {regionshape}
		if (Arena.regionmodify.equals("")) {
			Arenas.tellPlayer(player,
					Language.parse("regionnotbeingset", arena.name),
					arena);
			return;
		}

		if (arena.pos1 == null || arena.pos2 == null) {
			Arenas.tellPlayer(player, Language.parse("select2"), arena);
			return;
		}

		Vector realMin = new Vector(
				Math.min(arena.pos1.getBlockX(), arena.pos2.getBlockX()),
				Math.min(arena.pos1.getBlockY(), arena.pos2.getBlockY()),
				Math.min(arena.pos1.getBlockZ(), arena.pos2.getBlockZ()));
		Vector realMax = new Vector(
				Math.max(arena.pos1.getBlockX(), arena.pos2.getBlockX()),
				Math.max(arena.pos1.getBlockY(), arena.pos2.getBlockY()),
				Math.max(arena.pos1.getBlockZ(), arena.pos2.getBlockZ()));

		arena.pos1 = realMin.toLocation(Bukkit.getWorld(arena.getWorld()));
		arena.pos2 = realMax.toLocation(Bukkit.getWorld(arena.getWorld()));

		String s = arena.pos1.getBlockX() + "," + arena.pos1.getBlockY()
				+ "," + arena.pos1.getBlockZ() + "," + arena.pos2.getBlockX()
				+ "," + arena.pos2.getBlockY() + "," + arena.pos2.getBlockZ();

		ArenaRegion.RegionShape shape;
		
		if (args.length == 2) {
			shape = ArenaRegionManager.getShapeByName("cuboid");
		} else {
			shape = ArenaRegionManager.getShapeByName(args[2]);
		}

		// only cuboid if args = 2 | args[2] = cuboid

		ArenaRegion region = PVPArena.instance.getArm().newRegion(args[1], arena, arena.pos1,
				arena.pos2, shape);
		
		if (region == null) {
			Arenas.tellPlayer(sender, Language.parse("arenaregionshapeunknown", args[2]));
			Arenas.tellPlayer(sender, "§c/pa version regions");
			return;
		}
		
		if (region.getShape() != RegionShape.CUBOID) {
			s += "," + region.getName();
		}
		
		arena.cfg.set("regions." + args[1], s);
		arena.regions.put(args[1], region);
		arena.pos1 = null;
		arena.pos2 = null;
		
		arena.cfg.save();

		Arena.regionmodify = "";
		Arenas.tellPlayer(player, Language.parse("regionsaved"), arena);
	}

	@Override
	public String getName() {
		return "PAARegion";
	}
}
