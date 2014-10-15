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

import java.util.Date;

import javax.annotation.CheckForNull;

public final class Entity {

    public static Entity createCollection(final Path path, final Date lastModified) {
        return new Entity(path, Type.COLLECTION, null, lastModified, 0L);
    }

    public static Entity createItem(final Path path, final String hash, final Date lastModified, final long size) {
        return new Entity(path, Type.ITEM, hash, lastModified, size);
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
