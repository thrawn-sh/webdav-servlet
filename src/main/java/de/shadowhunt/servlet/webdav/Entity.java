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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public final class Entity {

    public static final PropertyIdentifier CONTENT_LENGTH_PROPERTY_IDENTIFIER;

    public static final PropertyIdentifier CONTENT_TYPE_PROPERTY_IDENTIFIER;

    public static final PropertyIdentifier CREATION_DATE_PROPERTY_IDENTIFIER;

    public static final PropertyIdentifier DISPLAY_NAME_PROPERTY_IDENTIFIER;

    public static final PropertyIdentifier ETAG_PROPERTY_IDENTIFIER;

    public static final PropertyIdentifier LAST_MODIFIED_PROPERTY_IDENTIFIER;

    public static final PropertyIdentifier LOCK_PROPERTY_IDENTIFIER;

    public static final PropertyIdentifier RESOURCE_TYPE_PROPERTY_IDENTIFIER;

    static {
        CREATION_DATE_PROPERTY_IDENTIFIER = new PropertyIdentifier(PropertyIdentifier.DAV_NAMESPACE, "creationdate");
        DISPLAY_NAME_PROPERTY_IDENTIFIER = new PropertyIdentifier(PropertyIdentifier.DAV_NAMESPACE, "displayname");
        CONTENT_LENGTH_PROPERTY_IDENTIFIER = new PropertyIdentifier(PropertyIdentifier.DAV_NAMESPACE, "getcontentlength");
        CONTENT_TYPE_PROPERTY_IDENTIFIER = new PropertyIdentifier(PropertyIdentifier.DAV_NAMESPACE, "getcontenttype");
        ETAG_PROPERTY_IDENTIFIER = new PropertyIdentifier(PropertyIdentifier.DAV_NAMESPACE, "getetag");
        LAST_MODIFIED_PROPERTY_IDENTIFIER = new PropertyIdentifier(PropertyIdentifier.DAV_NAMESPACE, "getlastmodified");
        RESOURCE_TYPE_PROPERTY_IDENTIFIER = new PropertyIdentifier(PropertyIdentifier.DAV_NAMESPACE, "resourcetype");
        LOCK_PROPERTY_IDENTIFIER = new PropertyIdentifier(PropertyIdentifier.DAV_NAMESPACE, "supportedlock");

        final Set<PropertyIdentifier> properties = new TreeSet<>();
        // properties.add(CREATION_DATE_PROPERTY); // not supported
        properties.add(DISPLAY_NAME_PROPERTY_IDENTIFIER); // getName
        properties.add(CONTENT_LENGTH_PROPERTY_IDENTIFIER); // getSize
        // properties.add(CONTENT_TYPE_PROPERTY); // not supported
        properties.add(ETAG_PROPERTY_IDENTIFIER); // getHash
        properties.add(LAST_MODIFIED_PROPERTY_IDENTIFIER); // getLastModified
        properties.add(RESOURCE_TYPE_PROPERTY_IDENTIFIER); // getScope
        properties.add(LOCK_PROPERTY_IDENTIFIER); // getLock
        SUPPORTED_LIVE_PROPERTIES = Collections.unmodifiableSet(properties);
    }

    public static final Set<PropertyIdentifier> SUPPORTED_LIVE_PROPERTIES;

    public static Entity createCollection(final Path path, final Date lastModified, @Nullable final Lock lock) {
        return new Entity(path, Type.COLLECTION, null, lastModified, 0L, lock);
    }

    public static Entity createItem(final Path path, final String hash, final Date lastModified, final long size, @Nullable final Lock lock) {
        return new Entity(path, Type.ITEM, hash, lastModified, size, lock);
    }

    public static Collection<Property> entityToProperties(final Entity entity) {
        final Collection<Property> result = new ArrayList<>();
        result.add(new StringProperty(DISPLAY_NAME_PROPERTY_IDENTIFIER, entity.getName()));
        result.add(new StringProperty(CONTENT_LENGTH_PROPERTY_IDENTIFIER, Long.toString(entity.getSize())));
        result.add(new StringProperty(LAST_MODIFIED_PROPERTY_IDENTIFIER, entity.getLastModified().toString())); // FIXME
        if (entity.getType() == Type.COLLECTION) {
            result.add(new Property(RESOURCE_TYPE_PROPERTY_IDENTIFIER) {

                @Override
                public void write(final XMLStreamWriter writer) throws XMLStreamException {
                    writer.writeStartElement(identifier.getNameSpace(), identifier.getName());
                    writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, "collection");
                    writer.writeEndElement();
                }
            });
        }

        final String hash = entity.getHash();
        if (hash != null) {
            result.add(new StringProperty(ETAG_PROPERTY_IDENTIFIER, hash));
        }

        final Lock lock = entity.getLock();
        if (lock != null) {

        }
        return result;

    }

    public static enum Type {
        COLLECTION(1), ITEM(2);

        public final int priority;

        private Type(final int priority) {
            this.priority = priority;
        }
    }

    private final String hash;

    private final Date lastModified;

    private final Lock lock;

    private final Path path;

    private final long size;

    private final Type type;

    private Entity(final Path path, final Type type, final String hash, final Date lastModified, final long size, @Nullable final Lock lock) {
        this.path = path;
        this.type = type;
        this.hash = hash;
        this.lastModified = new Date(lastModified.getTime());
        this.size = size;
        this.lock = lock;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entity)) {
            return false;
        }

        final Entity entity = (Entity) o;

        if (!path.equals(entity.path)) {
            return false;
        }
        if (type != entity.type) {
            return false;
        }

        return true;
    }

    /**
     * Returns a checksum of the resource
     *
     * @return the checksum of the resource or {@code null} if the resource is a collection
     */
    @CheckForNull
    public String getHash() {
        return hash;
    }

    public Date getLastModified() {
        return new Date(lastModified.getTime());
    }

    public Lock getLock() {
        return lock;
    }

    public String getName() {
        return path.getName();
    }

    /**
     * Returns a {@link Path} of the resource (relative to the root of the repository)
     *
     * @return the {@link Path} of the resource (relative to the root of the repository)
     */
    public Path getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public boolean isLocked() {
        return (lock != null);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Entity{");
        sb.append("hash='").append(hash).append('\'');
        sb.append(", path=").append(path);
        sb.append(", size=").append(size);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
