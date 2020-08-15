package net.slipcor.pvparena.classes;

public class PABlock {
    private final PABlockLocation location;
    private final String name;

    public PABlock(final PABlockLocation loc, final String string) {
        this.location = loc;
        this.name = string;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof PABlock) {
            final PABlock other = (PABlock) o;
            return this.name.equals(other.name) && this.location.equals(other.location);
        }
        return false;
    }

    public PABlockLocation getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }
}
