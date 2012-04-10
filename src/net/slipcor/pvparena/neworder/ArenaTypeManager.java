package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.List;

import com.nodinchan.loader.Loader;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.teams.TeamArena;

public class ArenaTypeManager {
	private final List<ArenaType> types;
	
	public ArenaTypeManager(PVPArena instance) {
		File path = new File(instance.getDataFolder().toString() + "/arenas");
		if (!path.exists()) {
			path.mkdir();
		}
		types = new Loader<ArenaType>(instance, path, new Object[] {}).load();
		types.add(new TeamArena());
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
