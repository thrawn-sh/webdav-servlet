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

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.concurrent.Immutable;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang3.Validate;

@Immutable
public final class PropertyIdentifier implements Comparable<PropertyIdentifier> {

    public static final PropertyIdentifier CONTENT_LENGTH_IDENTIFIER;

    public static final PropertyIdentifier CONTENT_TYPE_IDENTIFIER;

    public static final PropertyIdentifier CREATION_DATE_IDENTIFIER;

    public static final String DAV_NAMESPACE;

    public static final NamespaceContext DAV_NS_CONTEXT = new NamespaceContext() {

        @Override
        public String getNamespaceURI(final String prefix) {
            if (DEFAULT_DAV_PREFIX.equals(prefix)) {
                return DAV_NAMESPACE;
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

    public static final String DEFAULT_DAV_PREFIX;

    public static final PropertyIdentifier DISPLAY_NAME_IDENTIFIER;

    public static final PropertyIdentifier ETAG_IDENTIFIER;

    public static final PropertyIdentifier LAST_MODIFIED_IDENTIFIER;

    public static final PropertyIdentifier LOCK_IDENTIFIER;

    public static final PropertyIdentifier RESOURCE_TYPE_IDENTIFIER;

    public static final Set<PropertyIdentifier> SUPPORTED_LIVE_PROPERTIES;

    static {
        DAV_NAMESPACE = "DAV:";
        DEFAULT_DAV_PREFIX = "D";

        CREATION_DATE_IDENTIFIER = new PropertyIdentifier(DAV_NAMESPACE, "creationdate");
        DISPLAY_NAME_IDENTIFIER = new PropertyIdentifier(DAV_NAMESPACE, "displayname");
        CONTENT_LENGTH_IDENTIFIER = new PropertyIdentifier(DAV_NAMESPACE, "getcontentlength");
        CONTENT_TYPE_IDENTIFIER = new PropertyIdentifier(DAV_NAMESPACE, "getcontenttype");
        ETAG_IDENTIFIER = new PropertyIdentifier(DAV_NAMESPACE, "getetag");
        LAST_MODIFIED_IDENTIFIER = new PropertyIdentifier(DAV_NAMESPACE, "getlastmodified");
        RESOURCE_TYPE_IDENTIFIER = new PropertyIdentifier(DAV_NAMESPACE, "resourcetype");
        LOCK_IDENTIFIER = new PropertyIdentifier(DAV_NAMESPACE, "supportedlock");

        final Set<PropertyIdentifier> properties = new TreeSet<>();
        // properties.add(CREATION_DATE_PROPERTY); // not supported
        properties.add(DISPLAY_NAME_IDENTIFIER); // getName
        properties.add(CONTENT_LENGTH_IDENTIFIER); // getSize
        // properties.add(CONTENT_TYPE_PROPERTY); // not supported
        properties.add(ETAG_IDENTIFIER); // getEtag
        properties.add(LAST_MODIFIED_IDENTIFIER); // getLastModified
        properties.add(RESOURCE_TYPE_IDENTIFIER); // getScope
        properties.add(LOCK_IDENTIFIER); // getLock
        SUPPORTED_LIVE_PROPERTIES = Collections.unmodifiableSet(properties);
    }

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
