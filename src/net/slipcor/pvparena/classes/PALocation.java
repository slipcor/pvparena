package net.slipcor.pvparena.classes;

import net.slipcor.pvparena.core.StringParser;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * <pre>
 * PVP Arena Location class
 * </pre>
 * 
 * A simple wrapper of the Bukkit Location
 * 
 * @author slipcor
 * 
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
	public boolean equals(Object other) {
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		
		final PALocation location = (PALocation) other;
		
		if (this.world != location.world && (this.world == null || !this.world.equals(location.world))) {
			return false;
		}
		
		if (this.x != location.x) {
			return false;
		}
		
		if (this.y != location.y) {
			return false;
		}
		
		if (this.z != location.z) {
			return false;
		}
		
		if (this.pitch != location.pitch) {
			return false;
		}
		
		if (this.yaw != location.yaw) {
			return false;
		}
		
		return true;
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

		return Math.sqrt(Math.pow(this.x - otherLocation.x, 2.0D)
				+ Math.pow(this.y - otherLocation.y, 2.0D) + Math.pow(this.z - otherLocation.z, 2.0D));
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
		String[] aLoc = new String[6];
		aLoc[0] = "w:" + getWorldName();
		aLoc[1] = "x:" + getX();
		aLoc[2] = "y:" + getY();
		aLoc[3] = "z:" + getZ();
		aLoc[4] = "P:" + getPitch();
		aLoc[5] = "Y:" + getYaw();
		return StringParser.joinArray(aLoc, "|");
	}
}
