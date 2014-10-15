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
 * {@link Resource} defines a resource location in the repository
 */
public final class Resource implements Comparable<Resource> {

    /**
     * Represents the base {@link Resource} in the repository
     */
    public static final Resource ROOT = new Resource("");

    /**
     * Create a new {@link Resource} instance for the given value
     *
     * @param path value of the {@link Resource}
     *
     * @return the new {@link Resource} instance with the given value
     *
     * @throws IllegalArgumentException if the given path is not valid
     */
    public static Resource create(final String path) {
        if (StringUtils.isEmpty(path) || "/".equals(path)) {
            return ROOT;
        }

        final String normalizedPath = FilenameUtils.normalize(path, true);
        if (normalizedPath == null) {
            throw new IllegalArgumentException("invalid path: " + path);
        }

        final int lastCharacterIndex = normalizedPath.length() - 1;
        if (normalizedPath.charAt(lastCharacterIndex) == '/') {
            return new Resource(normalizedPath.substring(0, lastCharacterIndex));
        }
        return new Resource(normalizedPath);
    }

    private final String value;

    private Resource(final String value) {
        this.value = value;
    }

    /**
     * Appends the specified {@link Resource} to the end of this {@link Resource}
     *
     * @param resource the {@link Resource} that is appended to the end
     * of this {@link Resource}
     *
     * @return a {@link Resource} that represents the combination of this {@link Resource} and the specified {@link Resource}
     */
    public Resource append(final Resource resource) {
        return new Resource(value + resource.value);
    }

    @Override
    public int compareTo(final Resource other) {
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
        final Resource other = (Resource) obj;
        return value.equals(other.value);
    }

    public Resource getChild(final String name) {
        if (name.indexOf('/') >= 0) {
            throw new IllegalArgumentException("name must not contain path separator: " + name);
        }
        return new Resource(value + "/" + name);
    }

    public final String getName() {
        final int index = value.lastIndexOf('/');
        if (index < 0) {
            return value;
        }
        return value.substring(index + 1);
    }

    /**
     * Returns the parent {@link Resource} of the {@link Resource}, the parent of the ROOT element is the ROOT itself
     *
     * @return the parent {@link Resource} of the {@link Resource}
     */
    public Resource getParent() {
        if (equals(ROOT)) {
            return ROOT; // parent of root is root
        }

        final int indexOf = value.lastIndexOf('/');
        return new Resource(value.substring(0, indexOf));
    }

    /**
     * Returns a {@link String} representation of the {@link Resource}
     *
     * @return the {@link String} representation of the {@link Resource}
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
