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
package de.shadowhunt.servlet.aaa;

import java.util.Iterator;

import javax.annotation.concurrent.Immutable;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang3.Validate;

@Immutable
public final class PropertyIdentifier implements Comparable<PropertyIdentifier> {

    public static final String DAV_NAMESPACE = "DAV:";

    public static final NamespaceContext DAV_NS_CONTEXT = new NamespaceContext() {

        @Override
        public String getNamespaceURI(final String prefix) {
            if (DEFAULT_DAV_PREFIX.equals(prefix)) {
                return PropertyIdentifier.DAV_NAMESPACE;
            }
            throw new IllegalArgumentException(prefix);
        }

        @Override
        public String getPrefix(final String namespaceURI) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<?> getPrefixes(final String namespaceURI) {
            throw new UnsupportedOperationException();
        }
    };

    public static final String DEFAULT_DAV_PREFIX = "D";

    private final String name;

    private final String nameSpace;

    public PropertyIdentifier(final String nameSpace, final String name) {
        Validate.notNull(nameSpace, "nameSpace must not be null");
        Validate.notNull(name, "name must not be null");

        this.nameSpace = nameSpace;
        this.name = name;
    }

    @Override
    public int compareTo(final PropertyIdentifier o) {
        final int result = nameSpace.compareTo(o.nameSpace);
        if (result != 0) {
            return result;
        }

        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertyIdentifier)) {
            return false;
        }

        final PropertyIdentifier that = (PropertyIdentifier) o;

        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    public String getName() {
        return name;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + nameSpace.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return nameSpace + ":" + name;
    }
}
