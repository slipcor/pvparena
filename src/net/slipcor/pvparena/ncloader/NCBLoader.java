package net.slipcor.pvparena.ncloader;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.ncloader.NCBLoadable.LoadResult;
import net.slipcor.pvparena.ncloader.NCBLoadable.LoadResult.Result;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * Loader - Loader base for loading Loadables
 *
 * @param <T> A loadable class
 * @author NodinChan
 */
@SuppressWarnings("unchecked")
public class NCBLoader<T extends NCBLoadable> implements Listener {

    private final Plugin plugin;

    private final File dir;

    private ClassLoader loader;

    private final Object[] paramTypes;
    private final Class<?>[] ctorParams;

    private final List<File> files;
    private final List<T> loadables;

    public NCBLoader(final Plugin plugin, final File dir, final Object... paramTypes) {
        this.plugin = plugin;
        this.dir = dir;
        this.paramTypes = paramTypes;
        files = new ArrayList<>();
        loadables = new ArrayList<>();

        Collections.addAll(files, dir.listFiles(new FileExtensionFilter(".jar")));

        final List<Class<?>> constructorParams = new ArrayList<>();

        for (final Object paramType : paramTypes) {
            constructorParams.add(paramType.getClass());
        }

        ctorParams = constructorParams.toArray(new Class<?>[constructorParams.size()]);

        final List<URL> urls = new ArrayList<>();

        for (final File file : files) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]),
                plugin.getClass().getClassLoader());
    }

    /**
     * Gets the Logger
     *
     * @return The Logger
     */
    Logger getLogger() {
        return plugin.getLogger();
    }

    /**
     * Loads the Loadables
     *
     * @return List of loaded loadables
     */
    public final List<T> load(final Class<? extends NCBLoadable> classType) {
        for (final File file : files) {
            try {
                final JarFile jarFile = new JarFile(file);
                String mainClass = null;

                if (jarFile.getEntry("path.yml") != null) {
                    final JarEntry element = jarFile.getJarEntry("path.yml");
                    final BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    jarFile.getInputStream(element)));
                    mainClass = reader.readLine().substring(12);
                    if (mainClass.equals("net.slipcor.pvparena.modules.colorteams.CTManager")) {
                        PVPArena.instance.getLogger().warning("Skipping ColorTeams module! The functionality has been built into the main plugin!");
                        continue;
                    }
                    if (mainClass.equals("net.slipcor.pvparena.modules.scoreboards.ScoreBoards")) {
                        PVPArena.instance.getLogger().warning("Skipping ScoreBoards module! The functionality has been built into the main plugin!");
                        continue;
                    }
                }

                if (mainClass != null) {
                    final Class<?> clazz = Class.forName(mainClass, true, loader);

                    if (clazz != null) {
                        final Class<? extends NCBLoadable> loadableClass = clazz
                                .asSubclass(classType);
                        final Constructor<? extends NCBLoadable> ctor = loadableClass
                                .getConstructor(ctorParams);
                        final T loadable = (T) ctor.newInstance(paramTypes);



                        final LoadResult result = loadable.init();

                        if (result.getResult() == Result.SUCCESS) {
                            loadables.add(loadable);

                            final NCBLoadEvent<T> event = new NCBLoadEvent<>(plugin,
                                    loadable, jarFile);
                            plugin.getServer().getPluginManager()
                                    .callEvent(event);
                            continue;
                        }

                        final String reason = result.getReason();

                        if (reason != null && !reason.isEmpty()) {
                            getLogger().log(
                                    Level.INFO,
                                    "The JAR file " + file.getName()
                                            + " is not initialised: " + reason);
                        }
                    } else {
                        jarFile.close();
                        throw new ClassNotFoundException();
                    }

                } else {
                    jarFile.close();
                    throw new ClassNotFoundException();
                }

            } catch (final ClassCastException e) {
                e.printStackTrace();
                getLogger().log(
                        Level.WARNING,
                        "The JAR file " + file.getPath()
                                + " is in the wrong directory");
                getLogger().log(Level.WARNING,
                        "The JAR file " + file.getName() + " failed to load");

            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
                getLogger().log(Level.WARNING, "Invalid path.yml");
                getLogger().log(Level.WARNING,
                        "The JAR file " + file.getName() + " failed to load");

            } catch (final Exception e) {
                e.printStackTrace();
                getLogger().log(Level.WARNING, "Unknown cause");
                getLogger().log(Level.WARNING,
                        "The JAR file " + file.getName() + " failed to load");
            } catch (final NoClassDefFoundError ee) {
                ee.printStackTrace();
                getLogger().log(Level.WARNING,
                        "The JAR file " + file.getName() + " failed to load");
            }
        }

        return loadables;
    }

    /**
     * Reloads the Loader
     */
    public List<T> reload(final Class<? extends NCBLoadable> classType) {
        unload();

        final List<URL> urls = new ArrayList<>();
        files.clear();
        for (final String loadableFile : dir.list()) {
            if (loadableFile.endsWith(".jar")) {
                final File file = new File(dir, loadableFile);
                files.add(file);
                try {
                    urls.add(file.toURI().toURL());
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        loader = URLClassLoader.newInstance(urls.toArray(new URL[urls
                .size()]), plugin.getClass().getClassLoader());

        return load(classType);
    }

    /**
     * Unloads the Loader
     */
    void unload() {
        loadables.clear();
    }
}