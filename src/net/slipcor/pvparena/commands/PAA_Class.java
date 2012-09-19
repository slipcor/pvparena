package net.slipcor.pvparena.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Config.CFG;
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
 * @version v0.9.0
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
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{1,2})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}

		// /pa {arenaname} class save [name]
		// /pa {arenaname} class load [name]
		
		if (args.length == 1) {
			Player player = (Player) sender;
			try {
				arena.playerLeave(player, null);
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

			arena.getArenaConfig().setManually("classitems." + args[2], StringParser.getStringFromItemStacks(isItems));
			arena.getArenaConfig().save();
			arena.addClass(args[2], isItems);
			Arena.pmsg(player, Language.parse(MSG.CLASS_SAVED, args[2]));
		} else if (args[0].equalsIgnoreCase("load")) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
			arena.selectClass(ap, args[1]);
		} else if (args[0].equalsIgnoreCase("remove")) {
			Player player = (Player) sender;
			arena.getArenaConfig().setManually("classitems." + args[2], null);
			arena.getArenaConfig().save();
			arena.removeClass(args[2]);
			Arena.pmsg(player, Language.parse(MSG.CLASS_SAVED, args[2]));
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
