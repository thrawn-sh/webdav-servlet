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
package de.shadowhunt.webdav.store.memory;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavLock;

class MemoryEntity implements WebDavEntity {

    private final String etag;

    private final String hash;

    private final Date lastModified;

    private WebDavLock lock;

    private final String mimeType;

    private final WebDavPath path;

    private final long size;

    private final Type type;

    MemoryEntity(final WebDavPath path) {
        this.etag = null;
        this.hash = null;
        this.lastModified = new Date();
        this.mimeType = COLLECTION_MIME_TYPE;
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.size = 0L;
        this.type = Type.COLLECTION;
    }

    MemoryEntity(final WebDavPath path, final String hash, final long size, final String mimeType, final String etag) {
        this.etag = Objects.requireNonNull(etag, "etag must not be null");
        this.hash = Objects.requireNonNull(hash, "hash must not be null");
        this.lastModified = new Date();
        this.mimeType = Objects.requireNonNull(mimeType, "mimeType must not be null");
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.size = size;
        this.type = Type.ITEM;
    }

    @Override
    public int compareTo(final WebDavEntity other) {
        final int result = Integer.compare(type.priority, other.getType().priority);
        if (result != 0) {
            return result;
        }
        return path.compareTo(other.getPath());
    }

    void deleteLock() {
        this.lock = null;
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
        final MemoryEntity other = (MemoryEntity) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public Optional<String> getEtag() {
        return Optional.ofNullable(etag);
    }

    @Override
    public Optional<String> getHash() {
        return Optional.ofNullable(hash);
    }

    @Override
    public Date getLastModified() {
        return new Date(lastModified.getTime());
    }

    @Override
    public Optional<WebDavLock> getLock() {
        return Optional.ofNullable(lock);
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getName() {
        return path.getName();
    }

    @Override
    public WebDavPath getPath() {
        return path;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    void setLock(final WebDavLock lock) {
        this.lock = lock;
    }

    @Override
    public String toString() {
        return "MemoryEntity [path=" + path + ", type=" + type + ", etag=" + etag + ", hash=" + hash + ", lastModified=" + lastModified + ", lock=" + lock + ", mimeType=" + mimeType + ", size=" + size + "]";
    }
}
