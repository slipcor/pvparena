package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.definitions.Arena;

/**
 * setting manager class
 * 
 * -
 * 
 * provides access to the arena settings
 * 
 * @author slipcor
 * 
 * @version v0.6.1
 * 
 */

public class Settings {
	private final Arena arena;
	private static HashMap<String, String> types = new HashMap<String, String>();
	
	static {

		types.put("tp.win", "tp");
		types.put("tp.lose", "tp");
		types.put("tp.exit", "tp");
		types.put("tp.death", "tp");

		types.put("setup.wand", "int");
		
		types.put("game.lives", "int");
		types.put("game.preventDeath", "boolean");
		types.put("game.powerups", "string");
		types.put("game.refillInventory", "boolean");
		types.put("game.teamKill", "boolean");
		types.put("game.woolHead", "boolean");
		types.put("game.colorNick", "boolean");

		types.put("messages.chat", "boolean");
		types.put("messages.colorNick", "boolean");
		types.put("messages.defaultChat", "boolean");
		types.put("messages.language", "lang");
		
		types.put("general.classperms", "boolean");
		types.put("general.enabled", "boolean");
		types.put("general.owner", "string");
		types.put("general.world", "string");
		types.put("general.item-rewards", "items");

		types.put("arenatype.randomSpawn", "boolean");
		types.put("arenatype.teams", "boolean");
		types.put("arenatype.flags", "boolean");
		types.put("arenatype.pumpkin", "boolean");
		
		types.put("goal.timed", "int");

		types.put("join.forceEven", "boolean");
		types.put("join.manual", "boolean");
		types.put("join.random", "boolean");
		types.put("join.range", "int");
		
		types.put("periphery.checkRegions", "boolean");
		
		types.put("money.entry", "int");
		types.put("money.reward", "int");
		types.put("money.minbet", "double");
		types.put("money.maxbet", "double");
		
		types.put("protection.enabled", "boolean");
		types.put("protection.blockplace", "boolean");
		types.put("protection.blockdamage", "boolean");
		types.put("protection.firespread", "boolean");
		types.put("protection.lavafirespread", "boolean");
		types.put("protection.tnt", "boolean");
		types.put("protection.lighter", "boolean");
		types.put("protection.checkExit", "boolean");
		types.put("protection.checkSpectator", "boolean");
		types.put("protection.checkLounges", "boolean");

		types.put("start.health", "int");
		types.put("start.foodLevel", "int");
		types.put("start.saturation", "int");
		types.put("start.exhaustion", "double");

		types.put("ready.block", "item");
		types.put("ready.checkEach", "boolean");
		types.put("ready.min", "int");
		types.put("ready.max", "int");
		types.put("ready.minTeam", "int");
		types.put("ready.maxTeam", "int");

		types.put("announcements.join", "boolean");
		types.put("announcements.start", "boolean");
		types.put("announcements.end", "boolean");
		types.put("announcements.winner", "boolean");
		types.put("announcements.loser", "boolean");
		types.put("announcements.prize", "boolean");
		types.put("announcements.radius", "int");
		types.put("announcements.color", "string");
	}
	
	public Settings(Arena a) {
		arena = a;
	}

	public void list(Player player, int page) {
		if (page < 1) {
			page = 1;
		}
		Set<String> keys = new HashSet<String>();
		
		int i = 0;
		
		for (String node : arena.cfg.getYamlConfiguration().getKeys(true)) {
			if (types.get(node) == null) {
				continue;
			}
			System.out.print("reading...");
			if (i++ >= (page-1) * 10) {
				String[] s = node.split("\\.");
				keys.add(s[s.length-1]);
			}
			if (keys.size() >= 10) {
				break;
			}
		}
		Arenas.tellPlayer(player, ChatColor.GRAY + "------ config list ["+page+"] ------");
		for (String node : keys) {
			Arenas.tellPlayer(player, node + " => " + types.get(getNode(node)));
		}
		
	}
	
	private Object getNode(String node) {
		for (String s : arena.cfg.getYamlConfiguration().getKeys(true)) {

			if (types.get(s) == null) {
				continue;
			}
			
			if (s.endsWith("." + node)) {
				return s;
			}
		}
		return "null";
	}

	public void set(Player player, String node, String value) {
		
		if (!PVPArena.hasAdminPerms(player) && !PVPArena.hasCreatePerms(player, arena)) {

			Arenas.tellPlayer(player,
					PVPArena.lang.parse("nopermto", "set"));
			return;
		}
		
		for (String s : arena.cfg.getYamlConfiguration().getKeys(true)) {
			if (s.endsWith("." + node)) {
				set(player, s, value);
				return;
			}
		}
		
		String type = types.get(node);
		
		if (type == null) {
			type = "";
		}
		
		if (type.equals("boolean")) {
			if (value.equalsIgnoreCase("true")) {
				arena.cfg.set(node, Boolean.valueOf(true));
				Arenas.tellPlayer(player, node + " set to "+String.valueOf(value.equalsIgnoreCase("true")));
			} else if (value.equalsIgnoreCase("false")) {
				arena.cfg.set(node, Boolean.valueOf(false));
				Arenas.tellPlayer(player, node + " set to "+String.valueOf(value.equalsIgnoreCase("true")));
			} else {
				Arenas.tellPlayer(player, "No valid boolean '" + value + "'!");
				Arenas.tellPlayer(player, "Valid values: true | false");
				return;
			}
		} else if (type.equals("string")) {
			arena.cfg.set(node, String.valueOf(value));
			Arenas.tellPlayer(player, node + " set to "+String.valueOf(value));			
		} else if (type.equals("int")) {
			int i = 0;
			
			try {
				i = Integer.parseInt(value);
			} catch (Exception e) {
				Arenas.tellPlayer(player, "No valid int '"+value+"'! Use numbers without decimals!");
				return;
			}
			arena.cfg.set(node, i);
			Arenas.tellPlayer(player, node + " set to "+String.valueOf(i));
		} else if (type.equals("double")) {
			double d = 0;
			
			try {
				d = Double.parseDouble(value);
			} catch (Exception e) {
				Arenas.tellPlayer(player, "No valid double '"+value+"'! Use numbers with period (.)!");
				return;
			}
			arena.cfg.set(node, d);
			Arenas.tellPlayer(player, node + " set to "+String.valueOf(d));
		} else if (type.equals("tp")) {
			if (!value.equals("exit") && !value.equals("old") && !value.equals("spectator")) {
				Arenas.tellPlayer(player, "No valid tp '"+value+"'!");
				Arenas.tellPlayer(player, "Valid values: exit | old | spectator");
				return;
			}
			arena.cfg.set(node, String.valueOf(value));
			Arenas.tellPlayer(player, node + " set to "+String.valueOf(value));
		} else if (type.equals("item")) {
			try {
				try {
					Material mat = Material.valueOf(value);
					if (!mat.equals(Material.AIR)) {
						arena.cfg.set(node, mat.name());
						Arenas.tellPlayer(player, node + " set to "+String.valueOf(mat.name()));
					}
				} catch (Exception e2) {
					Material mat = Material.getMaterial(Integer.parseInt(value));
					arena.cfg.set(node, mat.name());
					Arenas.tellPlayer(player, node + " set to "+String.valueOf(mat.name()));
				}
				arena.cfg.save();
				return;
			} catch (Exception e) {
				//nothing
			}
			Arenas.tellPlayer(player, "No valid item '"+value+"'! Use valid ENUM or item id");
			return;
		} else if (type.equals("items")) {
			String[] ss = value.split(",");
			ItemStack[] items = new ItemStack[ss.length];

			for (int i = 0; i < ss.length; i++) {
				items[i] = arena.getItemStackFromString(ss[i], player);
				if (items[i] == null) {
					return;
				}
			}
			
			arena.cfg.set(node, String.valueOf(value));
			Arenas.tellPlayer(player, node + " set to "+String.valueOf(value));
		} else if (type.equals("lang")) {
			return; // TODO : lang support
		} else {
			Arenas.tellPlayer(player, "Unknown node: " + node);
			Arenas.tellPlayer(player, "use /pa [name] set [page] to get a node list");
			return;
		}
		arena.cfg.save();
	}
	
}
