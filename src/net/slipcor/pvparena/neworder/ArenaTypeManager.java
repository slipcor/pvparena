package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.List;

import com.nodinchan.loader.Loader;

import net.slipcor.pvparena.PVPArena;

public class ArenaTypeManager {
	private final List<ArenaType> types;
	
	public ArenaTypeManager(PVPArena instance) {
		File path = new File(instance.getDataFolder().toString() + "/arenas");
		if (!path.exists()) {
			path.mkdir();
		}
		types = new Loader<ArenaType>(instance, path, new Object[] {}).load();
	}
	
	public ArenaType getType(String tName) {
		for (ArenaType type : types) {
			System.out.print("type: " +type.getName() );
			if (type.getName().equals(tName)) {
				return type;
			}
		}
		return null;
	}
}
