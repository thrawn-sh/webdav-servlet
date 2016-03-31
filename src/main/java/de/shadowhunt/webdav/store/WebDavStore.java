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

import java.io.InputStream;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.method.WebDavMethod;
import de.shadowhunt.webdav.property.WebDavProperty;

@ThreadSafe
public interface WebDavStore {

    enum Access {
        ALLOW, DENY, REQUIRE_AUTHENTICATION;
    }

    void createCollection(WebDavPath path) throws WebDavException;

    void createItem(WebDavPath path, InputStream content) throws WebDavException;

    WebDavLockBuilder createLockBuilder();

    void delete(WebDavPath path) throws WebDavException;

    boolean exists(WebDavPath path) throws WebDavException;

    InputStream getContent(WebDavPath path) throws WebDavException;

    WebDavEntity getEntity(WebDavPath path) throws WebDavException;

    Collection<WebDavProperty> getProperties(WebDavPath path) throws WebDavException;

    Set<SupportedLock> getSupportedLocks(WebDavPath path) throws WebDavException;

    Access grantAccess(WebDavMethod method, WebDavPath path, Optional<Principal> principal);

    List<WebDavPath> list(WebDavPath path) throws WebDavException;

    WebDavEntity lock(WebDavPath path, WebDavLock lock) throws WebDavException;

    void setProperties(WebDavPath path, Collection<WebDavProperty> properties) throws WebDavException;

    void unlock(WebDavPath path) throws WebDavException;
}
