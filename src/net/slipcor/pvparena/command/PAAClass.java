package net.slipcor.pvparena.command;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PAAClass extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Arenas.tellPlayer(sender, Language.parse("onlyplayers"));
			return;
		}
		Player player = (Player) sender;
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("admin")), arena);
			return;
		}
		
		if (!checkArgs(player, args, 1, 3)) {
			return;
		}
		
		if (args.length < 2) {
			// exit the class editing
			/*
			Inventories.loadInventory(arena, player);
			*/
			
			try {
			arena.playerLeave(player, null);
			arena.remove(player);
			} catch (Exception e) {
				
			}
			ArenaPlayer.parsePlayer(player).setArena(null);
			
			return;
		}
		
		if (args[1].equals("save")) {
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
			
			arena.cfg.set("classes." + args[2], StringParser.getStringFromItemStacks(isItems));
			arena.cfg.save();
			arena.addClass(args[2], isItems);
			Arenas.tellPlayer(player, Language.parse("classsaved", args[2]));
		} else if (args[1].equals("preview")) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
			ap.setArena(arena);
			ap.setClass(args[2]);
			
			if (ap.getaClass() != null) {
				Inventories.prepareInventory(arena, player);
				Inventories.givePlayerFightItems(arena, player);

				Arenas.tellPlayer(player, Language.parse("classpreview", args[2]));
			} else {

				Arenas.tellPlayer(player, Language.parse("classunknown", args[2]));
			}
		} else if (args[1].equals("remove")) {
			arena.cfg.set("classes." + args[2], null);
			arena.cfg.save();
			arena.removeClass(args[2]);
			Arenas.tellPlayer(player, Language.parse("classremoved", args[2]));
		}
	}

	@Override
	public String getName() {
		return "PAACreate";
	}
}
