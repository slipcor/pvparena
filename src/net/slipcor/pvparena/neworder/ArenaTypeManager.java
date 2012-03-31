package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.List;

import com.nodinchan.loader.Loader;

import net.slipcor.pvparena.PVPArena;

public class ArenaTypeManager {
	private final List<ArenaType> types;
	
	public ArenaTypeManager(PVPArena instance) {
		types = new Loader<ArenaType>(instance, new File(instance.getDataFolder().toString() + "/plugins"), new Object[] {}).load();
	}
	
	public ArenaType getType(String tName) {
		for (ArenaType type : types) {
			if (type.getName().equals(type)) {
				return type;
			}
		}
		return null;
	}
}
