package net.slipcor.pvparena.ncloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
 * Loadable - Base for loadable classes
 * 
 * @author NodinChan
 * 
 */
public class NCBLoadable implements Cloneable {

	private final String name;

	private JarFile jar;

	public NCBLoadable(String name) {
		this.name = name;
	}

	@Override
	public NCBLoadable clone() {
		try {
			return (NCBLoadable) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Called when the Loadable is loaded by the Loader
	 * 
	 * @return True if the Loadable is initialised
	 */
	public LoadResult init() {
		return new LoadResult();
	}

	public boolean isInternal() {
		return false;
	}

	/**
	 * Gets the name of the Loadable
	 * 
	 * @return The name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Gets an embedded resource in this plugin
	 * 
	 * @param name
	 *            File name of the resource
	 * 
	 * @return InputStream of the file if found, otherwise null
	 */
	public InputStream getResource(String name) {
		ZipEntry entry = jar.getEntry(name);

		if (entry == null) {
			return null;
		}

		try {
			return jar.getInputStream(entry);
		} catch (IOException e) {
			return null;
		}
	}

	JarFile jar(JarFile jar) {
		return this.jar = jar;
	}

	/**
	 * Called when the Loadable is unloaded
	 */
	public void unload() {
	}

	public static final class LoadResult {

		private final Result result;

		private final String reason;

		public LoadResult() {
			this(Result.SUCCESS, "");
		}

		public LoadResult(String failReason) {
			this(Result.FAILURE, failReason);
		}

		public LoadResult(Result result, String reason) {
			this.result = result;
			this.reason = reason;
		}

		public String getReason() {
			return reason;
		}

		public Result getResult() {
			return result;
		}

		public enum Result {
			FAILURE, SUCCESS
		}
	}
}