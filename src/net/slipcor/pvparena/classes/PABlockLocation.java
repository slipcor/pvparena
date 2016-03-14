package net.slipcor.pvparena.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * <pre>
 * PVP Arena Location class
 * </pre>
 * <p/>
 * A simple wrapper of the Bukkit Location, only calculating blocks
 *
 * @author slipcor
 * @version v0.9.1
 */

public class PABlockLocation {
    private final String world;
    private int x;
    private int y;
    private int z;

    public PABlockLocation(final String world, final int x, final int y, final int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PABlockLocation(final String value) {
        String[] split = value.split(":");
        world = split[0];

        String[] ints = split[1].split(",");

        x = Integer.parseInt(ints[0]);
        y = Integer.parseInt(ints[1]);
        z = Integer.parseInt(ints[2]);
    }

    public PABlockLocation(final Location bukkitLocation) {
        world = bukkitLocation.getWorld().getName();
        x = bukkitLocation.getBlockX();
        y = bukkitLocation.getBlockY();
        z = bukkitLocation.getBlockZ();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (world == null ? 0 : world.hashCode());
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PABlockLocation other = (PABlockLocation) obj;
        if (world == null) {
            if (other.world != null) {
                return false;
            }
        } else if (!world.equals(other.world)) {
            return false;
        }
        if (x != other.x) {
            return false;
        }
        if (y != other.y) {
            return false;
        }
        return z == other.z;
    }

    public double getDistance(final PABlockLocation otherLocation) {
        if (otherLocation == null) {
            throw new IllegalArgumentException(
                    "Cannot measure distance to a null location");
        }
        if (!otherLocation.world.equals(world)) {
            throw new IllegalArgumentException(
                    "Cannot measure distance between " + world + " and "
                            + otherLocation.world);
        }

        return Math.sqrt(Math.pow(x - otherLocation.x, 2.0D)
                + Math.pow(y - otherLocation.y, 2.0D) + Math.pow(z - otherLocation.z, 2.0D));
    }

    public double getDistanceSquared(final PABlockLocation otherLocation) {
        if (otherLocation == null) {
            throw new IllegalArgumentException(
                    "Cannot measure distance to a null location");
        }
        if (!otherLocation.world.equals(world)) {
            throw new IllegalArgumentException(
                    "Cannot measure distance between " + world + " and "
                            + otherLocation.world);
        }

        return Math.pow(x - otherLocation.x, 2.0D)
                + Math.pow(y - otherLocation.y, 2.0D) + Math.pow(z - otherLocation.z, 2.0D);
    }

    public PABlockLocation getMidpoint(final PABlockLocation location) {
        return new PABlockLocation(world, (x + location.x) / 2, (y + location.y) / 2,
                (z + location.z) / 2);
    }

    public String getWorldName() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public boolean isInAABB(final PABlockLocation min, final PABlockLocation max) {
        if (x < min.x || x > max.x) {
            return false;
        }
        if (y < min.y || y > max.y) {
            return false;
        }
        return !(z < min.z || z > max.z);
    }

    public PABlockLocation pointTo(final PABlockLocation dest, final Double length) {
        final Vector source = new Vector(x, y, z);
        final Vector destination = new Vector(dest.x, dest.y, dest.z);

        Vector goal = source.subtract(destination);

        goal = goal.normalize().multiply(length);

        return new PABlockLocation(world, x + x + goal.getBlockX(), y
                + goal.getBlockY(), z + goal.getBlockZ());
    }

    public void setX(final int value) {
        x = value;
    }

    public void setY(final int value) {
        y = value;
    }

    public void setZ(final int value) {
        z = value;
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public String toString() {
        return world + ':' + x + ',' + y + ',' + z;
    }
}
