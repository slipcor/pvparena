package net.slipcor.pvparena.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * 
 * An own slim wrapper of the bukkit location
 * 
 * @author slipcor
 *
 */

public class PALocation {
	private String world;
	private double x;
	private double y;
	private double z;
	private float pitch;
	private float yaw;
	
	public PALocation(String world, double x, double y, double z, float pitch, float yaw) {
		this.world= world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	public PALocation(Location l) {
		world = l.getWorld().getName();
		x = l.getX();
		y = l.getY();
		z = l.getZ();
		pitch = l.getPitch();
		yaw = l.getYaw();
	}

	public PALocation add(double x, double y, double z) {
		return new PALocation(world, x+this.x, y+this.y, z+this.z, pitch, yaw);
	}

	public double getDistance(PALocation o) {
		if (o == null)
			throw new IllegalArgumentException(
					"Cannot measure distance to a null location");
		if (!o.world.equals(world)) {
			throw new IllegalArgumentException("Cannot measure distance between " + world + " and " + o.world);
		}
		
		return Math.sqrt(Math.pow(this.x - o.x, 2.0D) + Math.pow(this.y - o.y, 2.0D) + Math.pow(this.z - o.z, 2.0D));
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

	public void setPitch(float f) {
		pitch = f;
	}

	public void setX(double d) {
		x = d;
	}

	public void setY(double d) {
		y = d;
	}

	public void setYaw(float f) {
		yaw = f;
	}

	public void setZ(double d) {
		z = d;
	}

	public Location toLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
	}
}
