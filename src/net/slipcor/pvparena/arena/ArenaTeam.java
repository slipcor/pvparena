package net.slipcor.pvparena.arena;

import java.util.ArrayList;
import java.util.List;

public class ArenaTeam {
	
	private final List<ArenaPlayer> players;
	
	// yeah, I thought about adding that... ^^ this class will feature some string functions
	// coloring etc
	
	public ArenaTeam(String name) {
		this.players = new ArrayList<ArenaPlayer>();
	}
	
	public List<ArenaPlayer> getTeamMembers() {
		return players;
	}
}