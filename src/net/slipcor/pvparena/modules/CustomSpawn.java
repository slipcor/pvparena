package net.slipcor.pvparena.modules;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.loadables.ArenaModule;

public class CustomSpawn extends ArenaModule {

    public CustomSpawn() {
        super("CustomSpawn");
    }

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    @Override
    public boolean hasSpawn(final String s) {
        return true;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}
