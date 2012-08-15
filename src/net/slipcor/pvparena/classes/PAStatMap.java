package net.slipcor.pvparena.classes;

import java.util.HashMap;

import net.slipcor.pvparena.managers.Statistics;

public class PAStatMap {
	private final String playerName;
	private final HashMap<Statistics.type, Integer> map = new HashMap<Statistics.type, Integer>();
	
	public PAStatMap(String name) {
		playerName = name;
	}

	public void decStat(Statistics.type type) {
		decStat(type, 1);
	}

	public void decStat(Statistics.type type, int i) {
		map.put(type, getStat(type) - i);
	}
	
	public int getStat(Statistics.type type) {
		return map.containsKey(type)?map.get(type):0;
	}

	public void incStat(Statistics.type type) {
		incStat(type, 1);
	}

	public void incStat(Statistics.type type, int i) {
		map.put(type, getStat(type) + i);
	}
}
