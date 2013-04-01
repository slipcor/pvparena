package net.slipcor.pvparena.modules;

import net.slipcor.pvparena.loadables.ArenaModule;

public class CustomSpawn extends ArenaModule {

	public CustomSpawn() {
		super("CustomSpawn");
	}

	@Override
	public String version() {
		return "v1.0.1.96";
	}

	@Override
	public boolean hasSpawn(String s) {
		return true;
	}

	@Override
	public boolean isInternal() {
		return true;
	}
}
