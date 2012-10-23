package net.slipcor.pvparena.classes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
	private HashMap<Integer, PARound> rounds = new HashMap<Integer, PARound>();
	private Arena arena;
	
	public PARoundMap(Arena a, List<HashSet<String>> goalmap) {
		arena = a;
		int i = 1;
		for (HashSet<String> list : goalmap) {
			HashSet<ArenaGoal> result = new HashSet<ArenaGoal>();
			for (String s : list) {
				for (ArenaGoal goal : a.getGoals()) {
					if (goal.getName().equals(s)) {
						result.add(goal);
					}
				}
			}
			rounds.put(i++, new PARound(result));
		}
	}
	
	public HashSet<ArenaGoal> getGoals(int i) {
		try {
			return rounds.get(i).getGoals();
		} catch (Exception e) {
			arena.setRound(0);
			return arena.getGoals();
		}
	}

	public int getCount() {
		return rounds.size();
	}
	
	public PARound getRound(int i) {
		return rounds.get(i);
	}

	public void set(int i, PARound r) {
		rounds.put(i, r);
	}
}
