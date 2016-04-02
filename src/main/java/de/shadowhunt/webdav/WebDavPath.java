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
package de.shadowhunt.webdav;

import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * {@link WebDavPath} defines a path location in the repository.
 */
@Immutable
public final class WebDavPath implements Comparable<WebDavPath> {

    private static final Pattern PATH_PATTERN = Pattern.compile("/");

    /**
     * Represents the base {@link WebDavPath} in the repository.
     */
    public static final WebDavPath ROOT = new WebDavPath("");

    /**
     * Separator {@link String} for directories.
     */
    public static final String SEPARATOR = "/";

    /**
     * Separator {@code char} for directories.
     */
    public static final char SEPARATOR_CHAR = '/';

    /**
     * Create a new {@link WebDavPath} instance for the given value.
     *
     * @param path
     *            value of the {@link WebDavPath}
     *
     * @return the new {@link WebDavPath} instance with the given value
     */
    public static WebDavPath create(final String path) {
        if (StringUtils.isEmpty(path) || SEPARATOR.equals(path)) {
            return ROOT;
        }

        final StringBuilder sb = new StringBuilder();
        for (final String segment : PATH_PATTERN.split(path)) {
            if (StringUtils.isEmpty(segment) || ".".equals(segment)) {
                continue;
            }

            if ("..".equals(segment)) {
                throw new IllegalArgumentException("path is not canonical: " + path);
            }

            sb.append(SEPARATOR_CHAR);
            sb.append(segment);
        }

        return new WebDavPath(sb.toString());
    }

    private final String value;

    private WebDavPath(final String value) {
        this.value = value;
    }

    /**
     * Appends the specified {@link String} to the end of this {@link WebDavPath}.
     *
     * @param path
     *            the {@link String} that is appended to the end of this {@link WebDavPath}
     *
     * @return a {@link WebDavPath} that represents the combination of this {@link WebDavPath} and the specified {@link String}
     */
    public WebDavPath append(final String path) {
        Validate.notNull(path, "path must not be null");
        return create(this.value + SEPARATOR + path);
    }

    /**
     * Appends the specified {@link WebDavPath} to the end of this {@link WebDavPath}.
     *
     * @param path
     *            the {@link WebDavPath} that is appended to the end of this {@link WebDavPath}
     *
     * @return a {@link WebDavPath} that represents the combination of this {@link WebDavPath} and the specified {@link WebDavPath}
     */
    public WebDavPath append(final WebDavPath path) {
        return new WebDavPath(value + path.value);
    }

    @Override
    public int compareTo(final WebDavPath other) {
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
        final WebDavPath other = (WebDavPath) obj;
        return value.equals(other.value);
    }

    /**
     * Returns a {@link String} representation of the base name.
     *
     * @return {@link String} representation of the base name
     */
    public String getName() {
        final int index = value.lastIndexOf(SEPARATOR_CHAR);
        if (index < 0) {
            return value;
        }
        return value.substring(index + 1);
    }

    /**
     * Returns the parent {@link WebDavPath} of the {@link WebDavPath}, the parent of the ROOT element is the ROOT itself.
     *
     * @return the parent {@link WebDavPath} of the {@link WebDavPath}
     */
    public WebDavPath getParent() {
        if (equals(ROOT)) {
            return ROOT; // parent of root is root
        }
        final int indexOf = value.lastIndexOf(SEPARATOR_CHAR);
        return new WebDavPath(value.substring(0, indexOf));
    }

    /**
     * Returns the segment parts of the {@link WebDavPath}.
     * 
     * @return the segment parts of the {@link WebDavPath}
     */
    public String[] getSegments() {
        return PATH_PATTERN.split(value);
    }

    /**
     * Returns a {@link String} representation of the {@link WebDavPath}.
     *
     * @return the {@link String} representation of the {@link WebDavPath}
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
