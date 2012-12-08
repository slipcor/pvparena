package net.slipcor.pvparena.commands;

import java.util.HashMap;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help.HELP;
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
 * @version v0.10.0
 */

public class PAA_Set extends PAA__Command {

	public PAA_Set() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		if (!argCountValid(sender, arena, args, new Integer[]{1,2})) {
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
			if (s.toLowerCase().endsWith("." + node.toLowerCase())) {
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

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.SET));
	}
}
