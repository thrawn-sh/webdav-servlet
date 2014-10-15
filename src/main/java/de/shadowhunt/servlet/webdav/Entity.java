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

import java.util.Arrays;
import java.util.Date;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public final class Entity {

    private static final Date DEFAULT_DATE = new Date(0L);

    private Date lastModified = DEFAULT_DATE;

    private static final Property[] EMPTY = new Property[0];

    private Property[] properties = EMPTY;

    public static enum Type {
        COLLECTION(1), ITEM(2);

        public final int priority;

        private Type(final int priority) {
            this.priority = priority;
        }
    }

    // NOTE: not part of xml response but determined by a response header
    private String lockOwner = null;

    private String lockToken = null;

    private String md5 = null;

    private Path path = Path.ROOT;

    private long size = 0L;

    private Type type = Type.COLLECTION;

    public Date getLastModified() {
        return new Date(lastModified.getTime());
    }

    /**
     * Returns a name of the lock owner
     *
     * @return the name of the lock owner or {@code null} if the resource is not locked
     */
    @CheckForNull
    public String getLockOwner() {
        return lockOwner;
    }

    /**
     * Returns a lock-token
     *
     * @return the lock-token or {@code null} if the resource is not locked
     */
    @CheckForNull
    public String getLockToken() {
        return lockToken;
    }

    /**
     * Returns a MD5 checksum of the resource
     *
     * @return the MD5 checksum of the resource or {@code null} if the resource is a directory
     */
    @CheckForNull
    public String getMd5() {
        return md5;
    }

    public String getName() {
        return path.getName();
    }

    /**
     * Returns an array of the custom {@link Property}
     *
     * @return the array of the custom {@link Property} or an empty array if there a non
     */
    public Property[] getProperties() {
        return Arrays.copyOf(properties, properties.length);
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

    /**
     * Determines if the resource is locked
     *
     * @return {@code true} if the resource is locked otherwise {@code false}
     */
    public boolean isLocked() {
        return lockToken != null;
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = new Date(lastModified.getTime());
    }

    public void setLockOwner(@Nullable final String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public void setLockToken(@Nullable final String lockToken) {
        this.lockToken = lockToken;
    }

    public void setMd5(@Nullable final String md5) {
        this.md5 = md5;
    }

    public void setProperties(@Nullable final Property[] properties) {
        if ((properties == null) || (properties.length == 0)) {
            this.properties = EMPTY;
        } else {
            this.properties = Arrays.copyOf(properties, properties.length);
        }
    }

    public void setPath(final Path path) {
        this.path = path;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setType(final Type type) {
        this.type = type;
    }
}
