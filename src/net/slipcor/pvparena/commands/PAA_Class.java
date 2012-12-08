package net.slipcor.pvparena.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
 * <pre>PVP Arena CLASS Command class</pre>
 * 
 * A command to manage arena classes
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Class extends PAA__Command {

	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Class() {
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
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}

		// /pa {arenaname} class save [name]
		// /pa {arenaname} class load [name]
		// /pa {arenaname} class remove [name]
		
		if (args.length == 1) {
			Player player = (Player) sender;
			try {
				arena.playerLeave(player, null, true);
				arena.remove(player);
				} catch (Exception e) {

				}
				ArenaPlayer.parsePlayer(player.getName()).setArena(null);
				return;
		}
		
		if (args[0].equalsIgnoreCase("save")) {
			Player player = (Player) sender;
			List<ItemStack> items = new ArrayList<ItemStack>();

			for (ItemStack is : player.getInventory().getContents()) {
			if (is != null) {
			items.add(is);
			}
			}

			ItemStack[] isItems = new ItemStack[items.size()];
			int i = 0;
			for (ItemStack is : items) {
			isItems[i++] = is;
			}

			String sItems = (isItems == null || isItems.length < 1) ? "AIR" : StringParser.getStringFromItemStacks(isItems);
			
			arena.getArenaConfig().setManually("classitems." + args[1], sItems);
			arena.getArenaConfig().save();
			arena.addClass(args[1], isItems);
			Arena.pmsg(player, Language.parse(MSG.CLASS_SAVED, args[1]));
		} else if (args[0].equalsIgnoreCase("load")) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
			arena.selectClass(ap, args[1]);
		} else if (args[0].equalsIgnoreCase("remove")) {
			Player player = (Player) sender;
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
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.CLASS));
	}
}
