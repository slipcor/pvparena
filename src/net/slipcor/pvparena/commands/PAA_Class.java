package net.slipcor.pvparena.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(1,2)))) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse("command.onlyplayers"));
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
				ArenaPlayer.parsePlayer(player).setArena(null);
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

			arena.getArenaConfig().set("classitems." + args[2], StringParser.getStringFromItemStacks(isItems));
			arena.getArenaConfig().save();
			arena.addClass(args[2], isItems);
			Arena.pmsg(player, Language.parse("classsaved", args[2]));
		} else if (args[0].equalsIgnoreCase("load")) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
			arena.selectClass(ap, args[1]);
		} else if (args[0].equalsIgnoreCase("remove")) {
			Player player = (Player) sender;
			arena.getArenaConfig().set("classitems." + args[2], null);
			arena.getArenaConfig().save();
			arena.removeClass(args[2]);
			Arena.pmsg(player, Language.parse("classremoved", args[2]));
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
