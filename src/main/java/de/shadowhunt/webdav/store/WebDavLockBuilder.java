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
package de.shadowhunt.webdav.store;

import de.shadowhunt.webdav.WebDavConstant.Depth;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.store.WebDavLock.LockScope;
import de.shadowhunt.webdav.store.WebDavLock.LockType;
import de.shadowhunt.webdav.store.WebDavLock.Timeout;

public interface WebDavLockBuilder {

    WebDavLock build();

    void setDepth(Depth depth);

    void setOwner(String owner);

    void setRoot(WebDavPath root);

    void setScope(LockScope scope);

    void setTimeout(Timeout timeout);

    void setType(LockType type);
}
