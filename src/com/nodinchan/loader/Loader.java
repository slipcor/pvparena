package com.nodinchan.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unchecked")
/**
 * Loader - Loader base for loading Loadables
 * 
 * @author NodinChan
 *
 * @param <T> A loadable class
 */
public class Loader<T extends Loadable> implements Listener {
	
	private final Plugin plugin;
	
	private final ClassLoader loader;
	
	private final Object[] paramTypes;
	
	private final List<Class<?>> ctorParams;
	private final List<File> files;
	private final List<T> loadables;
	
	public Loader(Plugin plugin, File dir, Object[] paramTypes) {
		this.plugin = plugin;
		this.paramTypes = paramTypes;
		this.ctorParams = new ArrayList<Class<?>>();
		this.files = new ArrayList<File>();
		this.loadables = new ArrayList<T>();
		
		for (Object paramType : paramTypes) { ctorParams.add(paramType.getClass()); }
		
		List<URL> urls = new ArrayList<URL>();
		
		for (String loadableFile : dir.list()) {
			if (loadableFile.endsWith(".jar")) {
				File file = new File(dir, loadableFile);
				files.add(file);
				
				try { urls.add(file.toURI().toURL()); } catch (MalformedURLException e) { e.printStackTrace(); }
			}
		}
		
		this.loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), plugin.getClass().getClassLoader());
	}
	
	/**
	 * Gets the Logger
	 * 
	 * @return The Logger
	 */
	public Logger getLogger() {
		return plugin.getLogger();
	}
	
	/**
	 * Loads the Loadables
	 * 
	 * @return List of loaded loadables
	 */
	public List<T> load() {
		for (File file : files) {
			try {
				JarFile jarFile = new JarFile(file);
				Enumeration<JarEntry> entries = jarFile.entries();
				
				String mainClass = null;
				
				while (entries.hasMoreElements()) {
					JarEntry element = entries.nextElement();
					
					if (element.getName().equalsIgnoreCase("path.yml")) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
						mainClass = reader.readLine().substring(12);
						break;
					}
				}
				
				if (mainClass != null) {
					Class<?> clazz = Class.forName(mainClass, true, loader);
					Class<? extends Loadable> loadableClass = clazz.asSubclass(Loadable.class);
					Constructor<? extends Loadable> ctor = loadableClass.getConstructor(ctorParams.toArray(new Class<?>[0]));
					T loadable = (T) ctor.newInstance(paramTypes);
					
					LoadEvent event = new LoadEvent(plugin, loadable, jarFile);
					plugin.getServer().getPluginManager().callEvent(event);
					
					loadable.init();
					loadables.add(loadable);
					
				} else { throw new Exception(); }
				
			} catch (Exception e) {
				e.printStackTrace();
				getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
			}
		}
		
		return loadables;
	}
	
	/**
	 * Registers the Event Listener
	 * 
	 * @param loader The Loader to register
	 */
	public void register(Loader<T> loader) {
		plugin.getServer().getPluginManager().registerEvents(loader, plugin);
	}
	
	/**
	 * Sorts a list of Loadables
	 * 
	 * @param loadables The list of Loadables to sort
	 * 
	 * @return The sorted list of Loadables
	 */
	public List<T> sort(List<T> loadables) {
		List<T> sortedLoadables = new ArrayList<T>();
		List<String> names = new ArrayList<String>();
		
		for (T t : loadables) {
			names.add(t.getName());
		}
		
		Collections.sort(names);
		
		for (String name : names) {
			for (T t : loadables) {
				if (t.getName().equals(name))
					sortedLoadables.add(t);
			}
		}
		
		return sortedLoadables;
	}
}