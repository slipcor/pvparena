package net.slipcor.pvparena.command;

import java.util.HashMap;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.neworder.ArenaRegion;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class PAAInfo extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		// TODO reorganize and update
		String type = arena.type().getName();
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("       Arena Information about [" + ChatColor.AQUA
				+ arena.name + ChatColor.WHITE + "]");
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("Type: " + ChatColor.AQUA + type + ChatColor.WHITE
				+ " || " + "Teams: " + colorTeams(arena));
		player.sendMessage(StringParser.colorVar("Enabled",
				arena.cfg.getBoolean("general.enabled"))
				+ " || "
				+ StringParser.colorVar("Fighting", arena.fightInProgress)
				+ " || "
				+ "Wand: "
				+ Material.getMaterial(arena.cfg.getInt("setup.wand", 280))
						.toString()
				+ " || "
				+ "Timing: "
				+ StringParser.colorVar(arena.cfg.getInt("goal.timed"))
				+ " || "
				+ "MaxLives: "
				+ StringParser.colorVar(arena.cfg.getInt("game.lives", 3)));
		player.sendMessage("Regionset: "
				+ StringParser.colorVar(arena.name.equals(Arena.regionmodify))
				+ " || No Death: "
				+ StringParser.colorVar(arena.cfg
						.getBoolean("game.preventDeath"))
				+ " || "
				+ "Force: "
				+ StringParser.colorVar("Even",
						arena.cfg.getBoolean("join.forceEven", false))
				+ " | "
				+ StringParser.colorVar("Woolhead",
						arena.cfg.getBoolean("game.woolHead", false)));
		player.sendMessage(StringParser.colorVar("TeamKill",
				arena.cfg.getBoolean("game.teamKill", false))
				+ " || Team Select: "
				+ StringParser.colorVar("manual",
						arena.cfg.getBoolean("join.manual", true))
				+ " | "
				+ StringParser.colorVar("random",
						arena.cfg.getBoolean("join.random", true)));
		player.sendMessage("Regions: " + listRegions(arena.regions));
		player.sendMessage("TPs: exit: "
				+ StringParser.colorVar(arena.cfg.getString("tp.exit", "exit"))
				+ " | death: "
				+ StringParser.colorVar(arena.cfg.getString("tp.death",
						"spectator")) + " | win: "
				+ StringParser.colorVar(arena.cfg.getString("tp.win", "old"))
				+ " | lose: "
				+ StringParser.colorVar(arena.cfg.getString("tp.lose", "old")));
		PVPArena.instance.getAmm().parseInfo(arena, player);
		player.sendMessage(StringParser.colorVar("Protection",
				arena.cfg.getBoolean("protection.enabled", true))
				+ ": "
				+ StringParser.colorVar("Fire",
						arena.cfg.getBoolean("protection.firespread", true))
				+ " | "
				+ StringParser.colorVar("Destroy",
						arena.cfg.getBoolean("protection.blockdamage", true))
				+ " | "
				+ StringParser.colorVar("Place",
						arena.cfg.getBoolean("protection.blockplace", true))
				+ " | "
				+ StringParser.colorVar("Ignite",
						arena.cfg.getBoolean("protection.lighter", true))
				+ " | "
				+ StringParser.colorVar("Lava",
						arena.cfg.getBoolean("protection.lavafirespread", true))
				+ " | "
				+ StringParser.colorVar("Explode",
						arena.cfg.getBoolean("protection.tnt", true)));
		player.sendMessage(StringParser.colorVar("Check Regions",
				arena.cfg.getBoolean("periphery.checkRegions", false))
				+ ": "
				+ StringParser.colorVar("Exit",
						arena.cfg.getBoolean("protection.checkExit", false))
				+ " | "
				+ StringParser.colorVar("Lounges",
						arena.cfg.getBoolean("protection.checkLounges", false))
				+ " | "
				+ StringParser.colorVar("Spectator", arena.cfg.getBoolean(
						"protection.checkSpectator", false)));
		player.sendMessage("JoinRange: "
				+ StringParser.colorVar(arena.cfg.getInt("join.range", 0))
				+ " || Entry Fee: "
				+ StringParser.colorVar(arena.cfg.getInt("money.entry", 0))
				+ " || Reward: "
				+ StringParser.colorVar(arena.cfg.getInt("money.reward", 0))
				+ " || "
				+ StringParser.colorVar("refill",
						arena.cfg.getBoolean("game.refillInventory", false)));
	}

	/**
	 * turn a hashmap into a pipe separated string
	 * 
	 * @param arena
	 *            the input team map
	 * @return the joined and colored string
	 */
	private String colorTeams(Arena arena) {
		String s = "";
		for (ArenaTeam team : arena.getTeams()) {
			if (!s.equals("")) {
				s += " | ";
			}
			s += team.colorize() + ChatColor.WHITE;
		}
		return s;
	}

	/**
	 * turn a hashmap into a pipe separated strong
	 * 
	 * @param paRegions
	 *            the hashmap of regionname=>region
	 * @return the joined string
	 */
	private String listRegions(HashMap<String, ArenaRegion> paRegions) {
		String s = "";
		for (ArenaRegion p : paRegions.values()) {
			if (!s.equals("")) {
				s += " | ";
			}
			s += p.name + " (" + p.getShape().name().charAt(0) + ")";
		}
		return s;
	}
}
