package net.slipcor.pvparena.commands;

import java.util.HashMap;
import java.util.HashSet;


import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena LIST Command class</pre>
 * 
 * A command to display the players of an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAI_List extends PAA__Command {

	public PAI_List() {
		super(new String[] {"pvparena.user"});
	}
	
	private static HashMap<ArenaPlayer.Status, Character> colorMap = new HashMap<ArenaPlayer.Status, Character>();

	static {

		colorMap.put(ArenaPlayer.Status.NULL, 'm'); // error? strike through
		colorMap.put(ArenaPlayer.Status.WARM, '6'); // warm = gold
		colorMap.put(ArenaPlayer.Status.LOUNGE, 'b'); // readying up = aqua
		colorMap.put(ArenaPlayer.Status.READY, 'a'); // ready = green
		colorMap.put(ArenaPlayer.Status.FIGHT, 'f'); // fighting = white
		colorMap.put(ArenaPlayer.Status.WATCH, 'e'); // watching = yellow
		colorMap.put(ArenaPlayer.Status.DEAD, '7'); // dead = silver
		colorMap.put(ArenaPlayer.Status.LOST, 'c'); // lost = red
	}
	
	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0,1})) {
			return;
		}
		
		if (args.length < 1) {
		
			HashSet<String> names = new HashSet<String>();
			
			for (ArenaPlayer player : arena.getEveryone()) {
				names.add("&" + colorMap.get(player.getStatus()) + player.getName() + "&r");
			}
			arena.msg(sender, Language.parse(MSG.LIST_PLAYERS, StringParser.joinSet(names, ", ")));
			return;
		}
		
		HashMap<ArenaPlayer.Status, HashSet<String>> stats = new HashMap<ArenaPlayer.Status, HashSet<String>>();
		
		for (ArenaPlayer player : arena.getEveryone()) {
			HashSet<String> players = stats.containsKey(player.getStatus()) ? stats.get(player.getStatus()) : new HashSet<String>();
			
			players.add(player.getName());
			stats.put(player.getStatus(), players);
		}
		
		for (ArenaPlayer.Status stat : stats.keySet()) {
			arena.msg(sender, Language.parse(MSG.getByNode("LIST_" + stat.name()), "&" + colorMap.get(stat) + StringParser.joinSet(stats.get(stat), ", ")));
		}
		
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
