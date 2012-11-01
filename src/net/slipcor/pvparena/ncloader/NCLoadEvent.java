package net.slipcor.pvparena.ncloader;

import java.util.jar.JarFile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class NCLoadEvent extends Event
{
  private static final HandlerList handlers = new HandlerList();
  private final Plugin plugin;
  private final NCLoadable loadable;
  private final JarFile jarFile;

  public NCLoadEvent(Plugin plugin, NCLoadable loadable, JarFile jarFile)
  {
    this.plugin = plugin;
    this.loadable = loadable;
    this.jarFile = jarFile;
  }

  public HandlerList getHandlers()
  {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public JarFile getJarFile()
  {
    return this.jarFile;
  }

  public NCLoadable getLoadable()
  {
    return this.loadable;
  }

  public Plugin getPlugin()
  {
    return this.plugin;
  }
}