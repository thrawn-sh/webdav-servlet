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
package de.shadowhunt.webdav.method;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavLock;
import de.shadowhunt.webdav.store.WebDavStore;

import org.apache.commons.lang3.StringUtils;

public class UnlockMethod extends AbstractWebDavMethod {

    protected Optional<UUID> deterimineLockToken(final WebDavRequest request) {
        String token = request.getHeader(LockDiscoveryResponse.LOCK_TOKEN, "");
        if (StringUtils.isBlank(token)) {
            return Optional.empty();
        }

        if (token.charAt(0) != '<' || token.charAt(token.length() - 1) != '>') {
            return Optional.empty();
        }
        token = token.substring(1, token.length() - 1);

        if (!token.startsWith(WebDavLock.PREFIX)) {
            return Optional.empty();
        }
        token = token.substring(WebDavLock.PREFIX.length());

        try {
            return Optional.of(UUID.fromString(token));
        } catch (final RuntimeException e) {
            return Optional.empty();
        }
    }

    @Override
    public Method getMethod() {
        return Method.UNLOCK;
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavPath target = request.getPath();
        if (!store.exists(target)) {
            return AbstractBasicResponse.createNotFound();
        }

        final WebDavEntity entity = store.getEntity(target);
        final Optional<WebDavLock> lock = entity.getLock();
        if (!lock.isPresent()) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        final Optional<UUID> token = deterimineLockToken(request);
        if (!token.isPresent()) {
            return AbstractBasicResponse.createLocked(entity);
        }

        final WebDavLock webdavLock = lock.get();
        unlockRecurively(store, webdavLock.getRoot());
        return AbstractBasicResponse.createNoContent(entity);
    }

    private void unlockRecurively(final WebDavStore store, final WebDavPath path) {
        for (final WebDavPath child : store.list(path)) {
            unlockRecurively(store, child);
        }
        store.unlock(path);
    }
}
