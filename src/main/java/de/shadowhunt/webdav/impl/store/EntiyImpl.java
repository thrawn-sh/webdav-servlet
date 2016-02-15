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
package de.shadowhunt.webdav.impl.store;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Nullable;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.Lock;
import de.shadowhunt.webdav.Path;

public class EntiyImpl implements Entity {

    private final String hash;

    private final Date lastModified;

    private final Lock lock;

    private final Path path;

    private final long size;

    private final Type type;

    EntiyImpl(final Path path, final Type type, @Nullable final String hash, final Date lastModified, final long size, @Nullable final Lock lock) {
        this.path = path;
        this.type = type;
        this.hash = hash;
        this.lastModified = new Date(lastModified.getTime());
        this.size = size;
        this.lock = lock;
    }

    @Override
    public int compareTo(final Entity other) {
        return path.compareTo(other.getPath());
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
        final EntiyImpl other = (EntiyImpl) obj;
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
    public Optional<String> getHash() {
        return Optional.ofNullable(hash);
    }

    @Override
    public Date getLastModified() {
        return new Date(lastModified.getTime());
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getName() {
        return path.getName();
    }

    @Override
    public Path getPath() {
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

    @Override
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
