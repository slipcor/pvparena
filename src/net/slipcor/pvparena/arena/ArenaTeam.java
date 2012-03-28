package net.slipcor.pvparena.arena;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * arena team class
 * 
 * -
 * 
 * contains team methods and variables for quicker access
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class ArenaTeam {
	
	private final HashSet<ArenaPlayer> players;
	private final ChatColor color;
	private final String name;
	
	// yeah, I thought about adding that... ^^ this class will feature some string functions
	// coloring etc
	
	public ArenaTeam(String name, String color) {
		this.players = new HashSet<ArenaPlayer>();
		this.color = ChatColor.valueOf(name);
		this.name = name;
	}
	
	public HashSet<ArenaPlayer> getTeamMembers() {
		return players;
	}

	public void remove(ArenaPlayer player) {
		this.players.remove(player);
	}
	
	public void add(ArenaPlayer player) {
		this.players.add(player);
	}

	public ChatColor getColor() {
		return color;
	}

	public String colorize() {
		return color + name;
	}

	public String colorizePlayer(Player player) {
		return color + player.getName();
	}

	public String getName() {
		return name;
	}

	public String getColorString() {
		return "&"+Integer.toHexString(color.ordinal());
	}
}