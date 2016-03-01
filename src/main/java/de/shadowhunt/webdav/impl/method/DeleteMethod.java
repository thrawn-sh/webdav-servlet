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

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.WebDavStore;

public class DeleteMethod extends AbstractWebDavMethod {

    private void delete(final WebDavStore store, final WebDavPath path) {
        for (final WebDavPath child : store.list(path)) {
            delete(store, child);
        }
        store.delete(path);
    }

    @Override
    public Method getMethod() {
        return Method.DELETE;
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavPath target = request.getPath();
        if (WebDavPath.ROOT.equals(target)) {
            final WebDavEntity entity = store.getEntity(target);
            return AbstractBasicResponse.createForbidden(entity);
        }

        if (!store.exists(target)) {
            return AbstractBasicResponse.createNotFound();
        }

        delete(store, target);
        return AbstractBasicResponse.createNoContent(null);
    }
}
