package net.slipcor.pvparena.ncloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class NCLoader<T extends NCLoadable>
  implements Listener
{
  private final Plugin plugin;
  private final File dir;
  private ClassLoader loader;
  private final Object[] paramTypes;
  private final List<Class<?>> ctorParams;
  private final List<File> files;
  private final List<T> loadables;

  public NCLoader(Plugin plugin, File dir, Object[] paramTypes)
  {
    this.plugin = plugin;
    this.dir = dir;
    this.paramTypes = paramTypes;
    this.ctorParams = new ArrayList<Class<?>>();
    this.files = new ArrayList<File>();
    this.loadables = new ArrayList<T>();

    for (Object paramType : paramTypes) this.ctorParams.add(paramType.getClass());

    List<URL> urls = new ArrayList<URL>();

    for (String loadableFile : dir.list()) {
      if (loadableFile.endsWith(".jar")) {
        File file = new File(dir, loadableFile);
        this.files.add(file);
        try {
          urls.add(file.toURI().toURL()); } catch (MalformedURLException e) { e.printStackTrace();
        }
      }
    }
    this.loader = URLClassLoader.newInstance((URL[])urls.toArray(new URL[urls.size()]), plugin.getClass().getClassLoader());
  }

  public Logger getLogger()
  {
    return this.plugin.getLogger();
  }

  @SuppressWarnings("unchecked")
public final List<T> load()
  {
    for (File file : this.files) {
      try {
        JarFile jarFile = new JarFile(file);
        String mainClass = null;

        if (jarFile.getEntry("path.yml") != null) {
          JarEntry element = jarFile.getJarEntry("path.yml");
          BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
          mainClass = reader.readLine().substring(12);
        }

        if (mainClass != null) {
          Class<?> clazz = Class.forName(mainClass, true, this.loader);

          if (clazz != null) {
            Class<? extends NCLoadable> loadableClass = clazz.asSubclass(NCLoadable.class);
            Constructor<? extends NCLoadable> ctor = loadableClass.getConstructor((Class[])this.ctorParams.toArray(new Class[0]));
            T loadable = (T)ctor.newInstance(this.paramTypes);

            NCLoadEvent event = new NCLoadEvent(this.plugin, loadable, jarFile);
            this.plugin.getServer().getPluginManager().callEvent(event);

            loadable.init();
            this.loadables.add(loadable);
          } else {
            throw new ClassNotFoundException();
          }
        } else {
          throw new ClassNotFoundException();
        }
      } catch (ClassCastException e) {
        e.printStackTrace();
        getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " is in the wrong directory");
        getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
        getLogger().log(Level.WARNING, "Invalid path.yml");
        getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
      }
      catch (Exception e) {
        e.printStackTrace();
        getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
      }
    }

    return this.loadables;
  }

  public List<T> reload()
  {
    unload();

    List<URL> urls = new ArrayList<URL>();

    for (String loadableFile : this.dir.list()) {
      if (loadableFile.endsWith(".jar")) {
        File file = new File(this.dir, loadableFile);
        this.files.add(file);
        try {
          urls.add(file.toURI().toURL()); } catch (MalformedURLException e) { e.printStackTrace();
        }
      }
    }
    this.loader = URLClassLoader.newInstance((URL[])urls.toArray(new URL[urls.size()]), this.plugin.getClass().getClassLoader());

    return load();
  }

  public List<T> sort(List<T> loadables)
  {
    List<T> sortedLoadables = new ArrayList<T>();
    List<String> names = new ArrayList<String>();

    for (T t : loadables) {
      names.add(t.getName());
    }
    Collections.sort(names);

    for (String name : names) {
      for (T t : loadables) {
        if (t.getName().equals(name)) {
          sortedLoadables.add(t);
        }
      }
    }
    return sortedLoadables;
  }

  public Map<String, T> sort(Map<String, T> loadables)
  {
    Map<String, T> sortedLoadables = new HashMap<String, T>();
    List<String> names = new ArrayList<String>(loadables.keySet());

    Collections.sort(names);

    for (String name : names) {
      sortedLoadables.put(name, loadables.get(name));
    }
    return sortedLoadables;
  }

  public void unload()
  {
    this.loadables.clear();
    this.files.clear();
  }
}