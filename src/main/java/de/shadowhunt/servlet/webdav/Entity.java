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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Arrays;


public final class Entity {

    private static final Property[] EMPTY = new Property[0];

    private Property[] properties = EMPTY;

    // NOTE: not part of xml response but determined by a response header
    private String lockOwner = null;

    private String lockToken = null;

    private String md5 = null;

    private Resource resource = Resource.ROOT;

    private Type type = Type.FOLDER;

    private long size = 0L;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
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

    public void setLockOwner(@Nullable final String lockOwner) {
        this.lockOwner = lockOwner;
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

    public void setLockToken(@Nullable final String lockToken) {
        this.lockToken = lockToken;
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

    public void setMd5(@Nullable final String md5) {
        this.md5 = md5;
    }

    /**
     * Returns an array of the custom {@link Property}
     *
     * @return the array of the custom {@link Property} or an empty array if there a non
     */
    public Property[] getProperties() {
        return Arrays.copyOf(properties, properties.length);
    }

    public void setProperties(@Nullable final Property[] properties) {
        if ((properties == null) || (properties.length == 0)) {
            this.properties = EMPTY;
        } else {
            this.properties = Arrays.copyOf(properties, properties.length);
        }
    }

    /**
     * Returns a {@link Resource} of the resource (relative to the root of the repository)
     *
     * @return the {@link Resource} of the resource (relative to the root of the repository)
     */
    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    /**
     * Determines if the resource is locked
     *
     * @return {@code true} if the resource is locked otherwise {@code false}
     */
    public boolean isLocked() {
        return lockToken != null;
    }

    public static enum Type {
        FOLDER, FILE;
    }
}
