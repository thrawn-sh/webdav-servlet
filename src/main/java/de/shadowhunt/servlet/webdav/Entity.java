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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;

public final class Entity {

    public static final Property CONTENT_LENGTH_PROPERTY;

    public static final Property CONTENT_TYPE_PROPERTY;

    public static final Property CREATION_DATE_PROPERTY;

    public static final Property DISPLAY_NAME_PROPERTY;

    public static final Property ETAG_PROPERTY;

    public static final Property LAST_MODIFIED_PROPERTY;

    public static final Property LOCK_PROPERTY;

    public static final Property RESOURCE_TYPE_PROPERTY;

    static {
        CREATION_DATE_PROPERTY = new Property(Property.DAV_NAMESPACE, "creationdate");
        DISPLAY_NAME_PROPERTY = new Property(Property.DAV_NAMESPACE, "displayname");
        CONTENT_LENGTH_PROPERTY = new Property(Property.DAV_NAMESPACE, "getcontentlength");
        CONTENT_TYPE_PROPERTY = new Property(Property.DAV_NAMESPACE, "getcontenttype");
        ETAG_PROPERTY = new Property(Property.DAV_NAMESPACE, "getetag");
        LAST_MODIFIED_PROPERTY = new Property(Property.DAV_NAMESPACE, "getlastmodified");
        RESOURCE_TYPE_PROPERTY = new Property(Property.DAV_NAMESPACE, "resourcetype");
        LOCK_PROPERTY = new Property(Property.DAV_NAMESPACE, "supportedlock");

        final Set<Property> properties = new TreeSet<>();
        // properties.add(CREATION_DATE_PROPERTY); // not supported
        properties.add(DISPLAY_NAME_PROPERTY); // getName
        properties.add(CONTENT_LENGTH_PROPERTY); // getSize
        // properties.add(CONTENT_TYPE_PROPERTY); // not supported
        properties.add(ETAG_PROPERTY); // getHash
        properties.add(LAST_MODIFIED_PROPERTY); // getLastModified
        properties.add(RESOURCE_TYPE_PROPERTY); // getType
        //properties.add(LOCK_PROPERTY); // not supported
        SUPPORTED_LIVE_PROPERTIES = Collections.unmodifiableSet(properties);
    }

    public static final Set<Property> SUPPORTED_LIVE_PROPERTIES;

    public static Entity createCollection(final Path path, final Date lastModified) {
        return new Entity(path, Type.COLLECTION, null, lastModified, 0L);
    }

    public static Entity createItem(final Path path, final String hash, final Date lastModified, final long size) {
        return new Entity(path, Type.ITEM, hash, lastModified, size);
    }

    public static Map<Property, String> entityToProperties(final Entity entity) {
        final Map<Property, String> result = new HashMap<>();
        result.put(DISPLAY_NAME_PROPERTY, entity.getName());
        result.put(CONTENT_LENGTH_PROPERTY, Long.toString(entity.getSize()));
        result.put(ETAG_PROPERTY, entity.getHash());
        result.put(LAST_MODIFIED_PROPERTY, entity.getLastModified().toString()); // FIXME
        if (entity.getType() == Type.COLLECTION) {
            result.put(RESOURCE_TYPE_PROPERTY, "<D:collection/>");
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

    private final Path path;

    private final long size;

    private final Type type;

    private Entity(final Path path, final Type type, final String hash, final Date lastModified, final long size) {
        this.path = path;
        this.type = type;
        this.hash = hash;
        this.lastModified = new Date(lastModified.getTime());
        this.size = size;
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
