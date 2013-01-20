package net.slipcor.pvparena.classes;

import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.managers.StatisticsManager;

/**
 * <pre>PVP Arena Statistics Map class</pre>
 * 
 * A Map of Statistics, bound to a Player name, being sorted by Arena inside the Statistics class
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class PAStatMap {
	private final Map<StatisticsManager.type, Integer> map = new HashMap<StatisticsManager.type, Integer>();

	public void decStat(final StatisticsManager.type type) {
		decStat(type, 1);
	}

	public void decStat(final StatisticsManager.type type, final int value) {
		map.put(type, getStat(type) - value);
	}
	
	public int getStat(final StatisticsManager.type type) {
		return map.containsKey(type)?map.get(type):0;
	}

	public void incStat(final StatisticsManager.type type) {
		incStat(type, 1);
	}

	public void incStat(final StatisticsManager.type type, final int value) {
		map.put(type, getStat(type) + value);
	}

	public void setStat(final StatisticsManager.type type, final int value) {
		map.put(type, value);
	}
}
