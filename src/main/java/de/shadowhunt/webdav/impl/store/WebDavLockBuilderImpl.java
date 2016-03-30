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

import java.util.UUID;

import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavLock.LockScope;
import de.shadowhunt.webdav.WebDavLock.LockType;
import de.shadowhunt.webdav.WebDavLockBuilder;

public class WebDavLockBuilderImpl implements WebDavLockBuilder {

    private int depth = WebDavLock.INFINITY;

    private String owner = "";

    private LockScope scope = LockScope.EXCLUSIVE;

    private int timeoutInSeconds = WebDavLock.INFINITY;

    private LockType type = LockType.WRITE;

    @Override
    public WebDavLock build() {
        final UUID token = UUID.randomUUID();
        return new LockImpl(token, depth, scope, type, timeoutInSeconds, owner);
    }

    @Override
    public void setDepth(final int depth) {
        this.depth = depth;
    }

    @Override
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    @Override
    public void setScope(final LockScope scope) {
        this.scope = LockScope.EXCLUSIVE; // only exclusive locks are supported
    }

    @Override
    public void setTimeoutInSeconds(final int timeoutInSeconds) {
        this.timeoutInSeconds = -1; // only infinite locks are supported
    }

    @Override
    public void setType(final LockType type) {
        this.type = LockType.WRITE; // only write locks are supported
    }
}
