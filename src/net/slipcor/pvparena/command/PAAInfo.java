package net.slipcor.pvparena.command;

import java.util.HashMap;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.neworder.ArenaRegion;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class PAAInfo extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		String type = arena.type().getName();
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("Arena Information about: " + ChatColor.AQUA
				+ arena.name + "§f | [§a"+ arena.prefix +"§f]");
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("GameMode: " + ChatColor.AQUA + type + ChatColor.WHITE
				+ " || " + "Teams: " + colorTeams(arena));
		player.sendMessage("Classes: " + listClasses(arena));
		player.sendMessage("");
		
		player.sendMessage("§bRuntime:§f "+ StringParser.colorVar("Enabled",
				arena.cfg.getBoolean("general.enabled"))
				+ " || "
				+ StringParser.colorVar("EditMode", arena.edit)
				+ " || "
				+ StringParser.colorVar("Fighting", arena.fightInProgress)
				+ " || "
				+ StringParser.colorVar("preventDeath", arena.cfg
						.getBoolean("game.preventDeath"))+ " || "
				+ StringParser.colorVar("refill",
						arena.cfg.getBoolean("game.refillInventory", false)));
		
		player.sendMessage(
				"§bMessaging:§f ["
				+ StringParser.colorVar(arena.cfg.getString("messages.language"))
				+ "] || "
				+ StringParser.colorVar("chat",
				arena.cfg.getBoolean("messages.chat", false))
				+ " || "
				+ StringParser.colorVar("defaultChat",
				arena.cfg.getBoolean("messages.defaultChat", false))
				+ " || "
				+ StringParser.colorVar("onlyChat",
				arena.cfg.getBoolean("messages.onlyChat", false)));
		
		player.sendMessage("§bGeneral:§f " + StringParser.colorVar("classperms", 
				arena.cfg.getBoolean("general.classperms"))
				+ " || "
				+ StringParser.colorVar("signs", 
						arena.cfg.getBoolean("general.signs"))
				+ " || "
				+ StringParser.colorVar("random-reward", 
						arena.cfg.getBoolean("general.random-reward"))
				+ " || "
				+ "Wand: "
				+ Material.getMaterial(arena.cfg.getInt("setup.wand"))
										.toString());
		player.sendMessage(
				"Time limit: "
				+ StringParser.colorVar(arena.cfg.getInt("goal.timed"))
				+ " || "
				+ "MaxLives: "
				+ StringParser.colorVar(arena.cfg.getInt("game.lives"))
				+ " || "
				+ StringParser.colorVar("TeamKill",
				arena.cfg.getBoolean("game.teamKill", false))
				+ " || "
				+ StringParser.colorVar("woolHead",
						arena.cfg.getBoolean("game.woolHead", false)));
		player.sendMessage("");
		player.sendMessage("§bRegions:§f " + listRegions(arena.regions));
		player.sendMessage(StringParser.colorVar("Regionset", arena.name.equals(Arena.regionmodify))
				+ " || " 
				+ StringParser.colorVar("Check Regions", arena.cfg.getBoolean("periphery.checkRegions", false))
				+ ": "
				+ StringParser.colorVar("Exit",
						arena.cfg.getBoolean("protection.checkExit", false))
				+ " | "
				+ StringParser.colorVar("Lounges",
						arena.cfg.getBoolean("protection.checkLounges", false))
				+ " | "
				+ StringParser.colorVar("Spectator", arena.cfg.getBoolean(
						"protection.checkSpectator", false)));
		player.sendMessage("");
		player.sendMessage("§bJoining: "
				+ StringParser.colorVar("manual",
						arena.cfg.getBoolean("join.manual", true))
				+ " | "
				+ StringParser.colorVar("random",
						arena.cfg.getBoolean("join.random", true))
				+ " || "
				+ StringParser.colorVar("forceEven",
						arena.cfg.getBoolean("join.forceEven", false))
						+ " || "
						+ "Warmup: "
						+ StringParser.colorVar(arena.cfg.getInt("join.warmup", 0)));
		player.sendMessage(StringParser.colorVar("explicitPermission",
						arena.cfg.getBoolean("join.explicitPermission", true))
				+ " | "
				+ StringParser.colorVar("onCountdown",
						arena.cfg.getBoolean("join.onCountdown", true))
				+ " || "
				+ StringParser.colorVar("inbattle",
						arena.cfg.getBoolean("join.inbattle", false))
						+ " || "
						+ "JoinRange: "
						+ StringParser.colorVar(arena.cfg.getInt("join.range", 0)));
		player.sendMessage("");
		
		player.sendMessage(StringParser.colorVar("Protection",
				arena.cfg.getBoolean("protection.enabled", true))
				+ ": "
				+ StringParser.colorVar("firespread",
						arena.cfg.getBoolean("protection.firespread", true))
				+ " | "
				+ StringParser.colorVar("blockdamage",
						arena.cfg.getBoolean("protection.blockdamage", true))
				+ " | "
				+ StringParser.colorVar("blockplace",
						arena.cfg.getBoolean("protection.blockplace", true))
				+ " | "
				+ StringParser.colorVar("lighter",
						arena.cfg.getBoolean("protection.lighter", true))
				+ " | "
				+ StringParser.colorVar("tnt",
						arena.cfg.getBoolean("protection.tnt", true)));
		player.sendMessage(StringParser.colorVar("restore",
						arena.cfg.getBoolean("protection.restore", true))
				+ " | "
				+ StringParser.colorVar("lavafirespread",
						arena.cfg.getBoolean("protection.lavafirespread", true))
				+ " | "
				+ StringParser.colorVar("fluids",
						arena.cfg.getBoolean("protection.fluids", true))
				+ " | "
				+ StringParser.colorVar("punish",
						arena.cfg.getBoolean("protection.punish", true))
				+ " | "
				+ StringParser.colorVar("inventory",
						arena.cfg.getBoolean("protection.inventory", true))
				+ " | "
				+ "spawn: "
				+ StringParser.colorVar(arena.cfg.getInt("protection.spawn", 0)));
		player.sendMessage("");

		player.sendMessage("§bStart:§f "
				+ "countdown: "
				+ StringParser.colorVar(arena.cfg.getInt("start.countdown"))
				+ " | "
				+ "health: "
				+ StringParser.colorVar(arena.cfg.getInt("start.health"))
				+ " | "
				+ "foodLevel: "
				+ StringParser.colorVar(arena.cfg.getInt("start.foodLevel"))
				+ " | "
				+ "saturation: "
				+ StringParser.colorVar(arena.cfg.getInt("start.saturation"))
				+ " | "
				+ "exhaustion: "
				+ StringParser.colorVar(arena.cfg.getDouble("start.exhaustion")));
		

		player.sendMessage("§bReadying:§f "
				+ "block: "
				+ StringParser.colorVar(arena.cfg.getString("ready.block"))
				+ " | "
						+ StringParser.colorVar("checkEach", arena.cfg.getBoolean("ready.checkEach"))
				+ " | "
						+ StringParser.colorVar("checkEachTeam", arena.cfg.getBoolean("ready.checkEachTeam")));
		
		player.sendMessage("startRatio: "
				+ StringParser.colorVar(arena.cfg.getDouble("ready.startRatio"))
				+ " | min: "
				+ StringParser.colorVar(arena.cfg.getInt("ready.min"))
				+ " | minTeam: "
				+ StringParser.colorVar(arena.cfg.getInt("ready.minTeam"))
				+ " | max: "
				+ StringParser.colorVar(arena.cfg.getInt("ready.max"))
				+ " | maxTeam: "
				+ StringParser.colorVar(arena.cfg.getInt("ready.maxTeam"))
				+ " | autoclass: "
				+ StringParser.colorVar(arena.cfg.getString("ready.autoclass")));

		player.sendMessage("§bTPs:§f exit: "
				+ StringParser.colorVar(arena.cfg.getString("tp.exit", "exit"))
				+ " | death: "
				+ StringParser.colorVar(arena.cfg.getString("tp.death",
						"spectator")) + " | win: "
				+ StringParser.colorVar(arena.cfg.getString("tp.win", "old"))
				+ " | lose: "
				+ StringParser.colorVar(arena.cfg.getString("tp.lose", "old")));
		
		PVPArena.instance.getAmm().parseInfo(arena, player);
	}

	private String listClasses(Arena arena) {
		String result = "";
		for (ArenaClass c : arena.getClasses()) {
			if (c.getName().equals("custom")) {
				continue;
			}
			if (!result.equals("")) {
				result += "§f, §e";
			}
			result += "§e" + c.getName();
		}
		return result;
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
			s += p.name + " (" + p.getShape().name().substring(0,2) + ")";
		}
		return s;
	}

	@Override
	public String getName() {
		return "PAAInfo";
	}
}
