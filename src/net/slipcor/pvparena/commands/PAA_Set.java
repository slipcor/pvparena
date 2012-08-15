package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class PAA_Set extends PAA__Command {
	private static HashMap<String, String> types = new HashMap<String, String>();

	static {
		types.put("tp.win", "tp");
	}

	public PAA_Set() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(
				Arrays.asList(1, 2)))) {
			return;
		}

		// args[0]
		// usage: /pa {arenaname} set [page]

		if (args.length < 2) {
			try {
				int page = Integer.parseInt(args[0]);

				page = page < 1 ? 1 : page;

				Set<String> keys = new HashSet<String>();

				int i = 0;

				for (String node : arena.getArenaConfig().getYamlConfiguration()
						.getKeys(true)) {
					if (types.get(node) == null) {
						continue;
					}
					if (i++ >= (page - 1) * 10) {
						String[] s = node.split("\\.");
						keys.add(s[s.length - 1]);
					}
					if (keys.size() >= 10) {
						break;
					}
				}
				arena.msg(sender, "&6------ config list [" + page + "] ------");
				for (String node : keys) {
					arena.msg(sender,
							node + " => " + types.get(getNode(arena, node)));
				}

			} catch (Exception e) {
				arena.msg(sender, Language.parse("error.numeric", args[0]));
			}
			return;
		}

		// args[0]
		// usage: /pa {arenaname} set [node] [value]
		set(sender, arena, args[0], args[1]);
	}

	private String getNode(Arena arena, String node) {
		for (String s : arena.getArenaConfig().getYamlConfiguration().getKeys(true)) {

			if (types.get(s) == null) {
				continue;
			}

			if (s.endsWith("." + node)) {
				return s;
			}
		}
		return "null";
	}

	public void set(CommandSender player, Arena arena, String node, String value) {

		for (String s : arena.getArenaConfig().getYamlConfiguration().getKeys(true)) {
			if (s.endsWith("." + node)) {
				set(player, arena, s, value);
				return;
			}
		}

		String type = types.get(node);

		if (type == null) {
			type = "";
		}

		if (type.equals("boolean")) {
			if (value.equalsIgnoreCase("true")) {
				arena.getArenaConfig().set(node, Boolean.valueOf(true));
				arena.msg(
						player,
						Language.parse("set.done", node,
								String.valueOf(value.equalsIgnoreCase("true"))));
			} else if (value.equalsIgnoreCase("false")) {
				arena.getArenaConfig().set(node, Boolean.valueOf(false));
				arena.msg(
						player,
						Language.parse("set.done", node,
								String.valueOf(value.equalsIgnoreCase("true"))));
			} else {
				arena.msg(player, Language.parse("error.argumenttype", value,
						"boolean (true|false)"));
				return;
			}
		} else if (type.equals("string")) {
			arena.getArenaConfig().set(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse("set.done", node,
							String.valueOf(value)));
		} else if (type.equals("int")) {
			int i = 0;

			try {
				i = Integer.parseInt(value);
			} catch (Exception e) {
				arena.msg(player, Language.parse("error.numeric", value));
				return;
			}
			arena.getArenaConfig().set(node, i);
			arena.msg(
					player,
					Language.parse("set.done", node,
							String.valueOf(i)));
		} else if (type.equals("double")) {
			double d = 0;

			try {
				d = Double.parseDouble(value);
			} catch (Exception e) {
				arena.msg(player, Language.parse("error.argumenttype", value,
						"double (e.g. 12.00)"));
				return;
			}
			arena.getArenaConfig().set(node, d);
			arena.msg(
					player,
					Language.parse("set.done", node,
							String.valueOf(d)));
		} else if (type.equals("tp")) {
			if (!value.equals("exit") && !value.equals("old")
					&& !value.equals("spectator")) {
				arena.msg(player, Language.parse("error.argumenttype", value,
						"tp (exit|old|spectator|...)"));
				return;
			}
			arena.getArenaConfig().set(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse("set.done", node,
							String.valueOf(value)));
		} else if (type.equals("item")) {
			try {
				try {
					Material mat = Material.valueOf(value);
					if (!mat.equals(Material.AIR)) {
						arena.getArenaConfig().set(node, mat.name());
						arena.msg(
								player,
								Language.parse("set.done", node,
										String.valueOf(mat.name())));
					}
				} catch (Exception e2) {
					Material mat = Material
							.getMaterial(Integer.parseInt(value));
					arena.getArenaConfig().set(node, mat.name());
					arena.msg(
							player,
							Language.parse("set.done", node,
									String.valueOf(mat.name())));
				}
				arena.getArenaConfig().save();
				return;
			} catch (Exception e) {
				// nothing
			}
			arena.msg(player, Language.parse("error.argumenttype", value,
					"valid ENUM or item ID"));
			return;
		} else if (type.equals("items")) {
			String[] ss = value.split(",");
			ItemStack[] items = new ItemStack[ss.length];

			for (int i = 0; i < ss.length; i++) {
				items[i] = StringParser.getItemStackFromString(ss[i]);
				if (items[i] == null) {
					arena.msg(player, Language.parse("error.argumenttype", String.valueOf(items[i]),
							"item"));
					return;
				}
			}

			arena.getArenaConfig().set(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse("set.done", node,
							String.valueOf(value)));
		} else {
			arena.msg(
					player,
					Language.parse("set.unknown", node,
							String.valueOf(value)));
			arena.msg(
					player,
					Language.parse("set.help", node,
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
