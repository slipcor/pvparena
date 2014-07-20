package net.slipcor.pvparena.ncloader;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.util.jar.JarFile;

/*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * LoadEvent - Called when a Loadable has been loaded
 *
 * @author NodinChan
 */
class NCBLoadEvent<T> extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Plugin plugin;

    private final T loadable;

    private final JarFile jarFile;

    public NCBLoadEvent(final Plugin plugin, final T loadable, final JarFile jarFile) {
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
    public T getLoadable() {
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