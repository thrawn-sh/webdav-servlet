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
package de.shadowhunt.webdav.impl.method;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse.Status;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.precondition.Precondition;

abstract class AbstractWebDavMethod implements WebDavMethod {

    public static final String INFINITY = "infinity";

    static void checkDown(final WebDavStore store, final WebDavPath path, final int depth, final Map<WebDavPath, UUID> tokens) {
        if (depth < 0) {
            throw new WebDavException("no depth left to check child: " + path);
        }

        final WebDavEntity entity = store.getEntity(path);
        checkLockTokenOnEntity(entity, tokens);

        for (final WebDavPath child : store.list(path)) {
            checkDown(store, child, depth - 1, tokens);
        }
    }

    static void checkLockTokenOnEntity(final WebDavEntity entity, final Map<WebDavPath, UUID> tokens) {
        final Optional<WebDavLock> lock = entity.getLock();
        if (!lock.isPresent()) {
            return;
        }

        final UUID entityLockToken = lock.get().getToken();
        if (entityLockToken.equals(tokens.get(entity.getPath()))) {
            return;
        }
        throw new WebDavException("no suitable lock token provided", Status.SC_LOCKED);
    }

    static void checkUp(final WebDavStore store, final WebDavPath path, final Map<WebDavPath, UUID> tokens) {
        final WebDavEntity entity = store.getEntity(path);
        checkLockTokenOnEntity(entity, tokens);

        if (WebDavPath.ROOT.equals(path)) {
            return;
        }

        checkUp(store, path.getParent(), tokens);
    }

    protected Map<WebDavPath, UUID> deterimineLockTokens(final WebDavRequest request) {
        return Precondition.getTokens(request);
    }

    protected int determineDepth(final WebDavRequest request) {
        final String depth = request.getHeader("Depth", INFINITY);
        if (INFINITY.equalsIgnoreCase(depth)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(depth);
    }

    @Override
    public final String toString() {
        return "WebDavMethod [method=" + getMethod() + "]";
    }
}
