package net.slipcor.pvparena.classes;

import net.slipcor.pvparena.core.StringParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * <pre>
 * PVP Arena Location class
 * </pre>
 * <p/>
 * A simple wrapper of the Bukkit Location
 *
 * @author slipcor
 * @version v0.9.6
 */

public class PALocation {
    private final String world;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public PALocation(final String world, final double x, final double y, final double z, final float pitch,
                      final float yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public PALocation(final Location bukkitLocation) {
        world = bukkitLocation.getWorld().getName();
        x = bukkitLocation.getX();
        y = bukkitLocation.getY();
        z = bukkitLocation.getZ();
        pitch = bukkitLocation.getPitch();
        yaw = bukkitLocation.getYaw();
    }

    public PALocation add(final double x, final double y, final double z) {
        return new PALocation(world, x + this.x, y + this.y, z + this.z, pitch,
                yaw);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(pitch);
        result = prime * result + (world == null ? 0 : world.hashCode());
        long temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ temp >>> 32);
        result = prime * result + Float.floatToIntBits(yaw);
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ temp >>> 32);
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
        final PALocation other = (PALocation) obj;
        if (Float.floatToIntBits(pitch) != Float.floatToIntBits(other.pitch)) {
            return false;
        }
        if (world == null) {
            if (other.world != null) {
                return false;
            }
        } else if (!world.equals(other.world)) {
            return false;
        }
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Float.floatToIntBits(yaw) != Float.floatToIntBits(other.yaw)) {
            return false;
        }
        return Double.doubleToLongBits(z) == Double.doubleToLongBits(other.z);
    }

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    public double getDistance(final PALocation otherLocation) {
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

    public double getDistanceSquared(final PALocation otherLocation) {
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

    public double getPitch() {
        return pitch;
    }

    public String getWorldName() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getYaw() {
        return yaw;
    }

    public double getZ() {
        return z;
    }

    public void setPitch(final float value) {
        pitch = value;
    }

    public void setX(final double value) {
        x = value;
    }

    public void setY(final double value) {
        y = value;
    }

    public void setYaw(final float value) {
        yaw = value;
    }

    public void setZ(final double value) {
        z = value;
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        final String[] aLoc = new String[6];
        aLoc[0] = "w:" + world;
        aLoc[1] = "x:" + x;
        aLoc[2] = "y:" + y;
        aLoc[3] = "z:" + z;
        aLoc[4] = "P:" + getPitch();
        aLoc[5] = "Y:" + getYaw();
        return StringParser.joinArray(aLoc, "|");
    }
}
