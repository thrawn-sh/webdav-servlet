/**
 * This file is part of Shadowhunt WebDav Servlet.
 *
 * Shadowhunt WebDav Servlet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shadowhunt WebDav Servlet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shadowhunt WebDav Servlet.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.servlet.webdav;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * {@link Path} defines a resource location in the repository
 */
public final class Path implements Comparable<Path> {

    /**
     * Represents the base {@link Path} in the repository
     */
    public static final Path ROOT = new Path("");

    /**
     * Create a new {@link Path} instance for the given value
     *
     * @param path value of the {@link Path}
     *
     * @return the new {@link Path} instance with the given value
     *
     * @throws IllegalArgumentException if the given path is not valid
     */
    public static Path create(final String path) {
        if (StringUtils.isEmpty(path) || "/".equals(path)) {
            return ROOT;
        }

        final String normalizedPath = FilenameUtils.normalize(path, true);
        if (normalizedPath == null) {
            throw new IllegalArgumentException("invalid path: " + path);
        }

        final int lastCharacterIndex = normalizedPath.length() - 1;
        if (normalizedPath.charAt(lastCharacterIndex) == '/') {
            return new Path(normalizedPath.substring(0, lastCharacterIndex));
        }
        return new Path(normalizedPath);
    }

    private final String value;

    private Path(final String value) {
        this.value = value;
    }

    /**
     * Appends the specified {@link Path} to the end of this {@link Path}
     *
     * @param path the {@link Path} that is appended to the end
     * of this {@link Path}
     *
     * @return a {@link Path} that represents the combination of this {@link Path} and the specified {@link Path}
     */
    public Path append(final Path path) {
        return new Path(value + path.value);
    }

    @Override
    public int compareTo(final Path other) {
        Validate.notNull(other, "other must not be null");
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Path other = (Path) obj;
        return value.equals(other.value);
    }

    public Path getChild(final String name) {
        if (name.indexOf('/') >= 0) {
            throw new IllegalArgumentException("name must not contain path separator: " + name);
        }
        return new Path(value + "/" + name);
    }

    public final String getName() {
        final int index = value.lastIndexOf('/');
        if (index < 0) {
            return value;
        }
        return value.substring(index + 1);
    }

    /**
     * Returns the parent {@link Path} of the {@link Path}, the parent of the ROOT element is the ROOT itself
     *
     * @return the parent {@link Path} of the {@link Path}
     */
    public Path getParent() {
        if (equals(ROOT)) {
            return ROOT; // parent of root is root
        }

        final int indexOf = value.lastIndexOf('/');
        return new Path(value.substring(0, indexOf));
    }

    /**
     * Returns a {@link String} representation of the {@link Path}
     *
     * @return the {@link String} representation of the {@link Path}
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
