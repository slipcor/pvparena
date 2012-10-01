package net.slipcor.pvparena.classes;

import java.util.HashSet;

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
	private HashSet<ArenaGoal> goals = new HashSet<ArenaGoal>();
	
	public PARound(HashSet<ArenaGoal> arenagoals) {
		goals = arenagoals;
	}
	
	public HashSet<ArenaGoal> getGoals() {
		return goals;
	}
}
