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

public class MkColMethod extends AbstractWebDavMethod {

    @Override
    public Method getMethod() {
        return Method.MKCOL;
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavPath target = request.getPath();
        if (consume(request.getInputStream())) {
            WebDavEntity entity = null;
            if (store.exists(target)) {
                entity = store.getEntity(target);
            }
            return AbstractBasicResponse.createUnsupportedMediaType(entity);
        }

        final WebDavPath parent = target.getParent();
        if (!store.exists(parent)) {
            return AbstractBasicResponse.createConflict(null);
        }

        WebDavEntity entity = null;
        if (!store.exists(target)) {
            store.createCollection(target);
            entity = store.getEntity(target);
            return AbstractBasicResponse.createCreated(entity);
        }
        return AbstractBasicResponse.createMethodNotAllowed(entity);
    }
}
