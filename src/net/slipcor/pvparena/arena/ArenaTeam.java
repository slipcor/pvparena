package net.slipcor.pvparena.arena;

import java.util.ArrayList;
import java.util.List;

public class ArenaTeam {
	
	private final List<ArenaPlayer> players;
	
	public ArenaTeam(String name) {
		this.players = new ArrayList<ArenaPlayer>();
	}
	
	public List<ArenaPlayer> getTeamMembers() {
		return players;
	}
}