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

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.WebDavStore;

public class DeleteMethod extends AbstractWebDavMethod {

    static void delete(final WebDavStore store, final WebDavPath path, final int depth, final Set<UUID> tokens) {
        checkUp(store, path, tokens);
        for (final WebDavPath child : store.list(path)) {
            delete0(store, child, depth - 1, tokens);
        }
        store.delete(path);
    }

    static void delete0(final WebDavStore store, final WebDavPath path, final int depth, final Set<UUID> tokens) {
        if (depth < 0) {
            return; // FIXME
        }

        final WebDavEntity entity = store.getEntity(path);
        checkLockTokenOnEntity(entity, tokens);

        for (final WebDavPath child : store.list(path)) {
            delete(store, child, depth - 1, tokens);
        }
        store.delete(path);
    }

    @Override
    public Method getMethod() {
        return Method.DELETE;
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavPath path = request.getPath();
        if (WebDavPath.ROOT.equals(path)) {
            final WebDavEntity entity = store.getEntity(path);
            return AbstractBasicResponse.createForbidden(entity);
        }

        if (!store.exists(path)) {
            return AbstractBasicResponse.createNotFound();
        }

        final Set<UUID> tokens = deterimineLockTokens(request);

        final int depth = determineDepth(request);
        delete(store, path, depth, tokens);
        return AbstractBasicResponse.createNoContent(null);
    }
}
