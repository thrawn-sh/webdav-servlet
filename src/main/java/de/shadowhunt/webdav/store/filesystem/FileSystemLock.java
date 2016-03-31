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
package de.shadowhunt.webdav.store.filesystem;

import java.util.UUID;

import javax.annotation.concurrent.Immutable;

import de.shadowhunt.webdav.WebDavConstant.Depth;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.store.WebDavLock;

@Immutable
final class FileSystemLock implements WebDavLock {

    private final Depth depth;

    private final String owner;

    private final WebDavPath root;

    private final LockScope scope;

    private final int timeoutInSeconds;

    private final UUID token;

    private final LockType type;

    FileSystemLock(final UUID token, final WebDavPath root, final Depth depth, final LockScope scope, final LockType type, final int timeoutInSeconds, final String owner) {
        this.depth = depth;
        this.owner = owner;
        this.root = root;
        this.scope = scope;
        this.timeoutInSeconds = timeoutInSeconds;
        this.token = token;
        this.type = type;
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
        final FileSystemLock other = (FileSystemLock) obj;
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public Depth getDepth() {
        return depth;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public WebDavPath getRoot() {
        return root;
    }

    @Override
    public LockScope getScope() {
        return scope;
    }

    @Override
    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    @Override
    public UUID getToken() {
        return token;
    }

    @Override
    public LockType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "FileSystemLock [root=" + root + ", depth=" + depth + ", owner=" + owner + ", scope=" + scope + ", timeoutInSeconds=" + timeoutInSeconds + ", token=" + token + ", type=" + type + "]";
    }
}
