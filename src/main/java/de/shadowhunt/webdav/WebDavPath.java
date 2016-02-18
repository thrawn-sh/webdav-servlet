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

import javax.annotation.concurrent.Immutable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

@Immutable
public final class WebDavPath implements Comparable<WebDavPath> {

    public static final WebDavPath ROOT = new WebDavPath("");

    public static WebDavPath create(final String path) {
        if (StringUtils.isEmpty(path) || "/".equals(path)) {
            return ROOT;
        }

        final String normalizedPath = FilenameUtils.normalize(path, true);
        if (normalizedPath == null) {
            throw new IllegalArgumentException("invalid path: " + path);
        }

        final int lastCharacterIndex = normalizedPath.length() - 1;
        if (normalizedPath.charAt(lastCharacterIndex) == '/') {
            return new WebDavPath(normalizedPath.substring(0, lastCharacterIndex));
        }
        return new WebDavPath(normalizedPath);
    }

    private final String value;

    private WebDavPath(final String value) {
        this.value = value;
    }

    public WebDavPath append(final WebDavPath path) {
        Validate.notNull(path, "path must not be null");
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

    public WebDavPath getChild(final String name) {
        Validate.notNull(name, "name must not be null");
        if (name.indexOf('/') >= 0) {
            throw new IllegalArgumentException("name must not contain path separator: " + name);
        }
        return new WebDavPath(value + "/" + name);
    }

    public String getName() {
        final int index = value.lastIndexOf('/');
        if (index < 0) {
            return value;
        }
        return value.substring(index + 1);
    }

    public WebDavPath getParent() {
        if (equals(ROOT)) {
            return ROOT; // parent of root is root
        }

        final int indexOf = value.lastIndexOf('/');
        return new WebDavPath(value.substring(0, indexOf));
    }

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
