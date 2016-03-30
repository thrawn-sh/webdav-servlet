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
package de.shadowhunt.webdav;

import java.io.InputStream;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import de.shadowhunt.webdav.WebDavLock.LockScope;
import de.shadowhunt.webdav.WebDavLock.LockType;

@ThreadSafe
public interface WebDavStore {

    enum Access {
        ALLOW, DENY, REQUIRE_AUTHENTICATION;
    }

    class SupportedLock implements Comparable<SupportedLock> {

        private final LockScope scope;

        private final LockType type;

        SupportedLock(final LockScope scope, final LockType type) {
            this.scope = scope;
            this.type = type;
        }

        @Override
        public int compareTo(final SupportedLock other) {
            final int result = scope.compareTo(other.scope);
            if (result != 0) {
                return result;
            }
            return type.compareTo(other.type);
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
            final SupportedLock other = (SupportedLock) obj;
            if (scope != other.scope) {
                return false;
            }
            if (type != other.type) {
                return false;
            }
            return true;
        }

        public LockScope getScope() {
            return scope;
        }

        public LockType getType() {
            return type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((scope == null) ? 0 : scope.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "SupportedLock [scope=" + scope + ", type=" + type + "]";
        }
    }

    SupportedLock EXCLUSIV_WRITE_LOCK = new SupportedLock(LockScope.EXCLUSIVE, LockType.WRITE);

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
