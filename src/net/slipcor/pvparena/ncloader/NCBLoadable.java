package net.slipcor.pvparena.ncloader;

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
 */
public class NCBLoadable implements Cloneable {

    private final String name;

    protected NCBLoadable(final String name) {
        this.name = name;
    }

    @Override
    public NCBLoadable clone() {
        try {
            return (NCBLoadable) super.clone();
        } catch (final CloneNotSupportedException e) {
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

    public static final class LoadResult {

        private final Result result;

        private final String reason;

        public LoadResult() {
            this(Result.SUCCESS, "");
        }

        public LoadResult(final String failReason) {
            this(Result.FAILURE, failReason);
        }

        public LoadResult(final Result result, final String reason) {
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