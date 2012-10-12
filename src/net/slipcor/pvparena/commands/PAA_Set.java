package net.slipcor.pvparena.commands;

import java.util.HashMap;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

/**
 * <pre>PVP Arena SET Command class</pre>
 * 
 * A command to set config values
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_Set extends PAA__Command {
	//private static HashMap<String, String> types = new HashMap<String, String>();

	static {
/*
		types.put("tp.win", "tp");
		types.put("tp.lose", "tp");
		types.put("tp.exit", "tp");
		types.put("tp.death", "tp");

		types.put("setup.wand", "int");

		types.put("game.allowDrops", "boolean");
		types.put("game.dropSpawn", "boolean");
		types.put("game.lives", "int");
		types.put("game.preventDeath", "boolean");
		types.put("game.powerups", "string");
		types.put("game.refillInventory", "boolean");
		types.put("game.teamKill", "boolean");
		types.put("game.weaponDamage", "boolean");
		types.put("game.mustbesafe", "boolean");
		types.put("game.woolFlagHead", "boolean");

		types.put("messages.chat", "boolean");
		types.put("messages.colorNick", "boolean");
		types.put("messages.defaultChat", "boolean");
		types.put("messages.onlyChat", "boolean");
		types.put("messages.language", "lang");

		types.put("general.classperms", "boolean");
		types.put("general.cmdfailjoin", "boolean");
		types.put("general.enabled", "boolean");
		types.put("general.owner", "string");
		types.put("general.world", "string");
		types.put("general.item-rewards", "items");
		types.put("general.random-reward", "boolean");
		types.put("general.restoreChests", "boolean");
		types.put("general.signs", "boolean");

		types.put("region.spawncampdamage", "int");
		types.put("region.timer", "int");

		types.put("arenatype.randomSpawn", "boolean");

		types.put("goal.timed", "int");
		types.put("goal.endtimer", "int");

		types.put("join.explicitPermission", "boolean");
		types.put("join.forceEven", "boolean");
		types.put("join.inbattle", "boolean");
		types.put("join.manual", "boolean");
		types.put("join.onCountdown", "boolean");
		types.put("join.random", "boolean");
		types.put("join.range", "int");
		types.put("join.warmup", "int");

		types.put("periphery.checkRegions", "boolean");

		types.put("protection.spawn", "int");
		types.put("protection.restore", "boolean");
		types.put("protection.enabled", "boolean");
		
		types.put("protection.blockplace", "boolean");
		types.put("protection.blockdamage", "boolean");
		types.put("protection.blocktntdamage", "boolean");
		types.put("protection.decay", "boolean");
		types.put("protection.fade", "boolean");
		types.put("protection.form", "boolean");
		types.put("protection.fluids", "boolean");
		types.put("protection.firespread", "boolean");
		types.put("protection.grow", "boolean");
		types.put("protection.lavafirespread", "boolean");
		types.put("protection.lighter", "boolean");
		types.put("protection.painting", "boolean");
		types.put("protection.punish", "boolean");
		types.put("protection.piston", "boolean");
		types.put("protection.tnt", "boolean");
		
		types.put("protection.checkExit", "boolean");
		types.put("protection.checkSpectator", "boolean");
		types.put("protection.checkLounges", "boolean");
		types.put("protection.inventory", "boolean");

		types.put("start.countdown", "int");
		types.put("start.health", "int");
		types.put("start.foodLevel", "int");
		types.put("start.saturation", "int");
		types.put("start.exhaustion", "double");

		types.put("ready.block", "item");
		types.put("ready.checkEach", "boolean");
		types.put("ready.checkEachTeam", "boolean");
		types.put("ready.min", "int");
		types.put("ready.max", "int");
		types.put("ready.minTeam", "int");
		types.put("ready.maxTeam", "int");
		types.put("ready.autoclass", "string");
		types.put("ready.startRatio", "double"); */
		
	}

	public PAA_Set() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		if (!this.argCountValid(sender, arena, args, new Integer[]{1,2})) {
			return;
		}

		// args[0]
		// usage: /pa {arenaname} set [page]

		if (args.length < 2) {
			try {
				int page = Integer.parseInt(args[0]);

				page = page < 1 ? 1 : page;

				HashMap<String, String> keys = new HashMap<String, String>();

				int i = 0;

				for (String node : arena.getArenaConfig().getYamlConfiguration()
						.getKeys(true)) {
					if (CFG.getByNode(node) == null) {
						continue;
					}
					if (i++ >= (page - 1) * 10) {
						String[] s = node.split("\\.");
						keys.put(node, s[s.length - 1]);
					}
					if (keys.size() >= 10) {
						break;
					}
				}
				arena.msg(sender, "§6------ config list [" + page + "] ------");
				for (String node : keys.keySet()) {
					arena.msg(sender,
							keys.get(node) + " => " + CFG.getByNode(node).getType());
				}

			} catch (Exception e) {
				arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[0]));
			}
			return;
		}

		// args[0]
		// usage: /pa {arenaname} set [node] [value]
		set(sender, arena, args[0], args[1]);
	}

	public void set(CommandSender player, Arena arena, String node, String value) {

		for (String s : arena.getArenaConfig().getYamlConfiguration().getKeys(true)) {
			if (s.endsWith("." + node)) {
				set(player, arena, s, value);
				return;
			}
		}
		
		String type = CFG.getByNode(node) == null ? "" : CFG.getByNode(node).getType();


		if (type.equals("boolean")) {
			if (value.equalsIgnoreCase("true")) {
				arena.getArenaConfig().setManually(node, Boolean.valueOf(true));
				arena.msg(
						player,
						Language.parse(MSG.SET_DONE, node,
								String.valueOf(value.equalsIgnoreCase("true"))));
			} else if (value.equalsIgnoreCase("false")) {
				arena.getArenaConfig().setManually(node, Boolean.valueOf(false));
				arena.msg(
						player,
						Language.parse(MSG.SET_DONE, node,
								String.valueOf(value.equalsIgnoreCase("true"))));
			} else {
				arena.msg(player, Language.parse(MSG.ERROR_ARGUMENT_TYPE, value,
						"boolean (true|false)"));
				return;
			}
		} else if (type.equals("string")) {
			arena.getArenaConfig().setManually(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse(MSG.SET_DONE, node,
							String.valueOf(value)));
		} else if (type.equals("int")) {
			int i = 0;

			try {
				i = Integer.parseInt(value);
			} catch (Exception e) {
				arena.msg(player, Language.parse(MSG.ERROR_NOT_NUMERIC, value));
				return;
			}
			arena.getArenaConfig().setManually(node, i);
			arena.msg(
					player,
					Language.parse(MSG.SET_DONE, node,
							String.valueOf(i)));
		} else if (type.equals("double")) {
			double d = 0;

			try {
				d = Double.parseDouble(value);
			} catch (Exception e) {
				arena.msg(player, Language.parse(MSG.ERROR_ARGUMENT_TYPE, value,
						"double (e.g. 12.00)"));
				return;
			}
			arena.getArenaConfig().setManually(node, d);
			arena.msg(
					player,
					Language.parse(MSG.SET_DONE, node,
							String.valueOf(d)));
		} else if (type.equals("tp")) {
			if (!value.equals("exit") && !value.equals("old")
					&& !value.equals("spectator")) {
				arena.msg(player, Language.parse(MSG.ERROR_ARGUMENT_TYPE, value,
						"tp (exit|old|spectator|...)"));
				return;
			}
			arena.getArenaConfig().setManually(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse(MSG.SET_DONE, node,
							String.valueOf(value)));
		} else if (type.equals("item")) {
			try {
				try {
					Material mat = Material.valueOf(value);
					if (!mat.equals(Material.AIR)) {
						arena.getArenaConfig().setManually(node, mat.name());
						arena.msg(
								player,
								Language.parse(MSG.SET_DONE, node,
										String.valueOf(mat.name())));
					}
				} catch (Exception e2) {
					Material mat = Material
							.getMaterial(Integer.parseInt(value));
					arena.getArenaConfig().setManually(node, mat.name());
					arena.msg(
							player,
							Language.parse(MSG.SET_DONE, node,
									String.valueOf(mat.name())));
				}
				arena.getArenaConfig().save();
				return;
			} catch (Exception e) {
				// nothing
			}
			arena.msg(player, Language.parse(MSG.ERROR_ARGUMENT_TYPE, value,
					"valid ENUM or item ID"));
			return;
		} else if (type.equals("items")) {
			String[] ss = value.split(",");
			ItemStack[] items = new ItemStack[ss.length];

			for (int i = 0; i < ss.length; i++) {
				items[i] = StringParser.getItemStackFromString(ss[i]);
				if (items[i] == null) {
					arena.msg(player, Language.parse(MSG.ERROR_ARGUMENT_TYPE, String.valueOf(items[i]),
							"item"));
					return;
				}
			}

			arena.getArenaConfig().setManually(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse(MSG.SET_DONE, node,
							String.valueOf(value)));
		} else {
			arena.msg(
					player,
					Language.parse(MSG.SET_UNKNOWN, node,
							String.valueOf(value)));
			arena.msg(
					player,
					Language.parse(MSG.SET_HELP, node,
							String.valueOf(value)));
			return;
		}
		arena.getArenaConfig().save();
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
