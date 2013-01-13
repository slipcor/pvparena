package net.slipcor.pvparena.classes;

import java.util.HashSet;
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

public class PARound {
	private Set<ArenaGoal> goals = new HashSet<ArenaGoal>();
	
	public PARound(Set<ArenaGoal> result) {
		goals = result;
	}
	
	public Set<ArenaGoal> getGoals() {
		return goals;
	}

	public boolean toggle(Arena arena, ArenaGoal goal) {
		ArenaGoal nugoal = (ArenaGoal) goal.clone();
		nugoal.setArena(arena);
		
		boolean contains = false;
		ArenaGoal removeGoal = nugoal;
		
		for (ArenaGoal g : goals) {
			if (g.getName().equals(goal.getName())) {
				contains = true;
				removeGoal = g;
				break;
			}
		}
		
		if (contains) {
			goals.remove(removeGoal);
			arena.updateRounds();
			return false;
		} else {
			goals.add(nugoal);
			arena.updateRounds();
			return true;
		}
	}
}
