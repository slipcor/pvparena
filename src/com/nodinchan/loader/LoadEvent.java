package com.nodinchan.loader;

import java.util.jar.JarFile;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

/**
 * LoadEvent - Called when a Loadable has been loaded
 * 
 * @author NodinChan
 *
 */
public class LoadEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	
	private final Plugin plugin;
	
	private final Loadable loadable;
	
	private final JarFile jarFile;
	
	public LoadEvent(Plugin plugin, Loadable loadable, JarFile jarFile) {
		this.plugin = plugin;
		this.loadable = loadable;
		this.jarFile = jarFile;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * Gets the JAR file of the loaded loadable
	 * 
	 * @return The JAR file
	 */
	public JarFile getJarFile() {
		return jarFile;
	}
	
	/**
	 * Gets the loaded Loadable
	 * 
	 * @return The Loadable
	 */
	public Loadable getLoadable() {
		return loadable;
	}
	
	/**
	 * Gets the plugin calling this event
	 * 
	 * @return The plugin calling the event
	 */
	public Plugin getPlugin() {
		return plugin;
	}
}