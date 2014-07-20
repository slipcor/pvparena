package net.slipcor.pvparena.classes;

public class PASpawn {
    private final PALocation location;
    private final String name;

    public PASpawn(final PALocation loc, final String string) {
        location = loc;
        name = string;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof PASpawn) {
            final PASpawn other = (PASpawn) o;
            return name.equals(other.name) && location.equals(other.location);
        }
        return false;
    }

    public PALocation getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
