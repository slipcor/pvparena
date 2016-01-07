package net.slipcor.pvparena.classes;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.loadables.ArenaGoal;

import java.util.*;

/**
 * <pre>PVP Arena Round class</pre>
 * <p/>
 * A class to organize multiple ways of playing for an arena
 *
 * @author slipcor
 * @version v0.9.1
 */

public class PARoundMap {
    private final Map<Integer, PARound> rounds = new HashMap<>();
    private final Arena arena;

    public PARoundMap(final Arena arena, final List<Set<String>> outer) {
        this.arena = arena;
        int position = 1;
        for (final Set<String> list : outer) {
            final Set<ArenaGoal> result = new HashSet<>();
            for (final String s : list) {
                for (final ArenaGoal goal : arena.getGoals()) {
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
        } catch (final Exception e) {
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
