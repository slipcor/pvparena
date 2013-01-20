package net.slipcor.pvparena.classes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.loadables.ArenaGoal;

/**
 * <pre>PVP Arena Round class</pre>
 * 
 * A class to organize multiple ways of playing for an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PARoundMap {
	private final Map<Integer, PARound> rounds = new HashMap<Integer, PARound>();
	private final Arena arena;
	
	public PARoundMap(final Arena arena, final List<Set<String>> outer) {
		this.arena = arena;
		int position = 1;
		for (Set<String> list : outer) {
			final Set<ArenaGoal> result = new HashSet<ArenaGoal>();
			for (String s : list) {
				for (ArenaGoal goal : arena.getGoals()) {
					if (goal.getName().equals(s)) {
						result.add(goal);
					}
				}
			}
			rounds.put(position++, new PARound(result));
		}
	}
	
	public Set<ArenaGoal> getGoals(final int roundID) {
		try {
			return rounds.get(roundID).getGoals();
		} catch (Exception e) {
			arena.setRound(0);
			return arena.getGoals();
		}
	}

	public int getCount() {
		return rounds.size();
	}
	
	public PARound getRound(final int roundID) {
		return rounds.get(roundID);
	}

	public void set(final int roundID, final PARound round) {
		rounds.put(roundID, round);
	}
}
