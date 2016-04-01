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

import de.shadowhunt.webdav.WebDavConstant.Depth;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.store.WebDavLock;
import de.shadowhunt.webdav.store.WebDavLock.LockScope;
import de.shadowhunt.webdav.store.WebDavLock.LockType;
import de.shadowhunt.webdav.store.WebDavLock.Timeout;
import de.shadowhunt.webdav.store.WebDavLockBuilder;

public class FileSystemLockBuilder implements WebDavLockBuilder {

    private Depth depth = Depth.INFINITY;

    private String owner = "";

    private WebDavPath root = null;

    private LockScope scope = LockScope.EXCLUSIVE;

    private Timeout timeout = Timeout.INFINITE;

    private LockType type = LockType.WRITE;

    @Override
    public WebDavLock build() {
        final UUID token = UUID.randomUUID();
        if (root == null) {
            throw new WebDavException("lock root must be defined");
        }
        return new FileSystemLock(token, root, depth, scope, type, timeout, owner);
    }

    @Override
    public void setDepth(final Depth depth) {
        this.depth = depth;
    }

    @Override
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    @Override
    public void setRoot(final WebDavPath root) {
        this.root = root;
    }

    @Override
    public void setScope(final LockScope scope) {
        this.scope = LockScope.EXCLUSIVE; // only exclusive locks are supported
    }

    @Override
    public void setTimeout(final Timeout timeout) {
        this.timeout = Timeout.INFINITE; // only infinite locks are supported
    }

    @Override
    public void setType(final LockType type) {
        this.type = LockType.WRITE; // only write locks are supported
    }
}
