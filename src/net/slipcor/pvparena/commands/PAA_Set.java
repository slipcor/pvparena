package net.slipcor.pvparena.commands;

import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

public class PAA_Set extends AbstractArenaCommand {

	public PAA_Set() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
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

				final Map<String, String> keys = new HashMap<String, String>();

				int position = 0;

				for (String node : arena.getArenaConfig().getYamlConfiguration()
						.getKeys(true)) {
					if (CFG.getByNode(node) == null) {
						continue;
					}
					if (position++ >= (page - 1) * 10) {
						final String[] split = node.split("\\.");
						keys.put(node, split[split.length - 1]);
					}
					if (keys.size() >= 10) {
						break;
					}
				}
				arena.msg(sender, ChatColor.COLOR_CHAR + "6------ config list [" + page + "] ------");
				for (String node : keys.keySet()) {
					arena.msg(sender,
							keys.get(node) + " => " + CFG.getByNode(node).getType());
				}

			} catch (Exception e) {
				arena.msg(sender, Language.parse(arena, MSG.ERROR_NOT_NUMERIC, args[0]));
			}
			return;
		}

		// args[0]
		// usage: /pa {arenaname} set [node] [value]
		set(sender, arena, args[0], args[1]);
	}

	private void set(final CommandSender player, final Arena arena, final String node, final String value) {

		for (String s : arena.getArenaConfig().getYamlConfiguration().getKeys(true)) {
			if (s.toLowerCase().endsWith("." + node.toLowerCase())) {
				set(player, arena, s, value);
				return;
			}
		}
		
		final String type = CFG.getByNode(node) == null ? "" : CFG.getByNode(node).getType();


		if (type.equals("boolean")) {
			if (value.equalsIgnoreCase("true")) {
				arena.getArenaConfig().setManually(node, Boolean.TRUE);
				arena.msg(
						player,
						Language.parse(arena, MSG.SET_DONE, node,
								String.valueOf(value.equalsIgnoreCase("true"))));
			} else if (value.equalsIgnoreCase("false")) {
				arena.getArenaConfig().setManually(node, Boolean.FALSE);
				arena.msg(
						player,
						Language.parse(arena, MSG.SET_DONE, node,
								String.valueOf(value.equalsIgnoreCase("true"))));
			} else {
				arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, value,
						"boolean (true|false)"));
				return;
			}
		} else if (type.equals("string")) {
			arena.getArenaConfig().setManually(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse(arena, MSG.SET_DONE, node,
							String.valueOf(value)));
		} else if (type.equals("int")) {
			int iValue = 0;

			try {
				iValue = Integer.parseInt(value);
			} catch (Exception e) {
				arena.msg(player, Language.parse(arena, MSG.ERROR_NOT_NUMERIC, value));
				return;
			}
			arena.getArenaConfig().setManually(node, iValue);
			arena.msg(
					player,
					Language.parse(arena, MSG.SET_DONE, node,
							String.valueOf(iValue)));
		} else if (type.equals("double")) {
			double dValue = 0;

			try {
				dValue = Double.parseDouble(value);
			} catch (Exception e) {
				arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, value,
						"double (e.g. 12.00)"));
				return;
			}
			arena.getArenaConfig().setManually(node, dValue);
			arena.msg(
					player,
					Language.parse(arena, MSG.SET_DONE, node,
							String.valueOf(dValue)));
		} else if (type.equals("tp")) {
			if (!value.equals("exit") && !value.equals("old")
					&& !value.equals("spectator")) {
				arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, value,
						"tp (exit|old|spectator|...)"));
				return;
			}
			arena.getArenaConfig().setManually(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse(arena, MSG.SET_DONE, node,
							String.valueOf(value)));
		} else if (type.equals("material")) {
			if (value.equals("hand")) {
				if (player instanceof Player) {

					final Material mat = ((Player) player).getItemInHand().getType();
					arena.getArenaConfig().setManually(node, mat.name());
					arena.msg(
							player,
							Language.parse(arena, MSG.SET_DONE, node,
									String.valueOf(mat.name())));
				} else {
					arena.msg(player, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
				}
				return;
			}
			
			try {
				try {
					final Material mat = Material.valueOf(value);
					if (!mat.equals(Material.AIR)) {
						arena.getArenaConfig().setManually(node, mat.name());
						arena.msg(
								player,
								Language.parse(arena, MSG.SET_DONE, node,
										String.valueOf(mat.name())));
					}
				} catch (Exception e2) {
					final Material mat = Material
							.getMaterial(Integer.parseInt(value));
					arena.getArenaConfig().setManually(node, mat.name());
					arena.msg(
							player,
							Language.parse(arena, MSG.SET_DONE, node,
									String.valueOf(mat.name())));
				}
				arena.getArenaConfig().save();
				return;
			} catch (Exception e) {
				arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, value,
						"valid ENUM or item ID"));
			}
			return;
		} else if (type.equals("items")) {
			if (value.equals("inventory")) {
				if (player instanceof Player) {

					String newValue = StringParser.getStringFromItemStacks(((Player) player).getInventory().getContents());
					arena.getArenaConfig().setManually(node, newValue);
					arena.msg(
							player,
							Language.parse(arena, MSG.SET_DONE, node,
									newValue));
				} else {
					arena.msg(player, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
				}
				return;
			}
			
			final String[] split = value.split(",");
			ItemStack[] items = new ItemStack[split.length];

			for (int i = 0; i < split.length; i++) {
				items[i] = StringParser.getItemStackFromString(split[i]);
				if (items[i] == null) {
					arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, String.valueOf(items[i]),
							"item"));
					return;
				}
			}

			arena.getArenaConfig().setManually(node, String.valueOf(value));
			arena.msg(
					player,
					Language.parse(arena, MSG.SET_DONE, node,
							String.valueOf(value)));
		} else {
			arena.msg(
					player,
					Language.parse(arena, MSG.SET_UNKNOWN, node,
							String.valueOf(value)));
			arena.msg(
					player,
					Language.parse(arena, MSG.SET_HELP, node,
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
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.SET));
	}
}
