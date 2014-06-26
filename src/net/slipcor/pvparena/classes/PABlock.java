package net.slipcor.pvparena.classes;

public class PABlock {
    private final PABlockLocation location;
    private final String name;

    public PABlock(PABlockLocation loc, String string) {
        location = loc;
        name = string;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof PABlock) {
            final PABlock other = (PABlock) o;
            return name.equals(other.name) && location.equals(other.location);
        }
        return false;
    }

    public PABlockLocation getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
