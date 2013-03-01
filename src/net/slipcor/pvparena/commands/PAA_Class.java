package net.slipcor.pvparena.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * <pre>
 * PVP Arena CLASS Command class
 * </pre>
 * 
 * A command to manage arena classes
 * 
 * @author slipcor
 * 
 * @version v0.10.1
 */

public class PAA_Class extends AbstractArenaCommand {

	public static Map<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Class() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		if (!argCountValid(sender, arena, args, new Integer[] { 1, 2 })) {
			return;
		}

		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}

		// /pa {arenaname} class save [name]
		// /pa {arenaname} class load [name]
		// /pa {arenaname} class remove [name]

		if (args.length == 1) {
			final Player player = (Player) sender;
			PVPArena.instance.getLogger().info("Exiting edit mode: " + player.getName());
			
			ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

			ArenaPlayer.reloadInventory(arena, player);
			
			aPlayer.setArena(null);
			return;
		}

		if (args[0].equalsIgnoreCase("save")) {
			final Player player = (Player) sender;
			final List<ItemStack> items = new ArrayList<ItemStack>();

			for (ItemStack is : player.getInventory().getArmorContents()) {
				if (is != null) {
					items.add(is);
				}
			}
			
			for (ItemStack is : player.getInventory().getContents()) {
				if (is != null) {
					items.add(is);
				}
			}

			ItemStack[] isItems = new ItemStack[items.size()];
			int position = 0;
			for (ItemStack is : items) {
				isItems[position++] = is;
			}

			final String sItems = (isItems == null || isItems.length < 1) ? "AIR"
					: StringParser.getStringFromItemStacks(isItems);

			arena.getArenaConfig().setManually("classitems." + args[1], sItems);
			arena.getArenaConfig().save();
			arena.addClass(args[1], isItems);
			Arena.pmsg(player, Language.parse(MSG.CLASS_SAVED, args[1]));
		} else if (args[0].equalsIgnoreCase("load")) {
			final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());
			

			ArenaPlayer.backupAndClearInventory(arena, aPlayer.get());
			
			arena.selectClass(aPlayer, args[1]);
		} else if (args[0].equalsIgnoreCase("remove")) {
			final Player player = (Player) sender;
			arena.getArenaConfig().setManually("classitems." + args[1], null);
			arena.getArenaConfig().save();
			arena.removeClass(args[1]);
			Arena.pmsg(player, Language.parse(MSG.CLASS_REMOVED, args[1]));
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.CLASS));
	}
}
