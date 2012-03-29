package net.slipcor.pvparena.arena;

import java.util.ArrayList;
import java.util.List;

public final class Arena {
	
	// will integrate the following, hoping people don't disappear again
	
	private final List<ArenaPlayer> players;
	
	public Arena(String name, String type) {
		this.players = new ArrayList<ArenaPlayer>();
	}
	
	public List<ArenaPlayer> getPlayers() {
		return players;
	}
}