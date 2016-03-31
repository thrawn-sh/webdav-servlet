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
import java.io.InputStream;

import javax.annotation.CheckForNull;

import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavStore;

public class MkColMethod extends AbstractWebDavMethod {

    protected boolean consume(@CheckForNull final InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return false;
        }

        final boolean data = (inputStream.read() != -1);
        if (data) {
            while (inputStream.read() != -1) {
                // just deplete inputStream
            }
        }
        return data;
    }

    @Override
    public Method getMethod() {
        return Method.MKCOL;
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavPath path = request.getPath();
        if (consume(request.getInputStream())) {
            WebDavEntity entity = null;
            if (store.exists(path)) {
                entity = store.getEntity(path);
            }
            return AbstractBasicResponse.createUnsupportedMediaType(entity);
        }

        final WebDavPath parent = path.getParent();
        if (!store.exists(parent)) {
            return AbstractBasicResponse.createConflict(null);
        }

        if (!store.exists(path)) {
            checkUp(store, parent, determineLockTokens(request));
            store.createCollection(path);
            final WebDavEntity entity = store.getEntity(path);
            return AbstractBasicResponse.createCreated(entity);
        }

        final WebDavEntity entity = store.getEntity(path);
        return AbstractBasicResponse.createMethodNotAllowed(entity);
    }
}
