package net.slipcor.pvparena.classes;

import java.util.HashMap;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.StatisticsManager;

/**
 * <pre>PVP Arena Statistics Map class</pre>
 * 
 * A Map of Statistics, bound to a Player name, being sorted by Arena inside the Statistics class
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAStatMap {
	private final String playerName;
	private final Debug db = new Debug(12);
	private final HashMap<StatisticsManager.type, Integer> map = new HashMap<StatisticsManager.type, Integer>();
	
	public PAStatMap(String name) {
		playerName = name;
		db.i("created player stat map for " + playerName);
	}

	public void decStat(StatisticsManager.type type) {
		decStat(type, 1);
	}

	public void decStat(StatisticsManager.type type, int i) {
		map.put(type, getStat(type) - i);
	}
	
	public int getStat(StatisticsManager.type type) {
		return map.containsKey(type)?map.get(type):0;
	}

	public void incStat(StatisticsManager.type type) {
		incStat(type, 1);
	}

	public void incStat(StatisticsManager.type type, int i) {
		map.put(type, getStat(type) + i);
	}

	public void setStat(StatisticsManager.type type, int i) {
		map.put(type, i);
	}
}
