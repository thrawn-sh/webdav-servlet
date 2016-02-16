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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.Path;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavStore;

public class MkColMethod extends AbstractWebDavMethod {

    @Override
    public Method getMethod() {
        return Method.MKCOL;
    }

    @Override
    public WebDavResponse service(final WebDavStore store, final Path path, final HttpServletRequest request) throws ServletException, IOException {
        if (consume(request.getInputStream())) {
            Entity entity = null;
            if (store.exists(path)) {
                entity = store.getEntity(path);
            }
            return AbstractBasicResponse.createUnsupportedMediaType(entity);
        }

        final Path parent = path.getParent();
        if (!store.exists(parent)) {
            return AbstractBasicResponse.createConflict(null);
        }

        Entity entity = null;
        if (!store.exists(path)) {
            store.createCollection(path);
            entity = store.getEntity(path);
            return AbstractBasicResponse.createCreated(entity);
        }
        return AbstractBasicResponse.createMethodNotAllowed(entity);
    }
}