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

public class UnlockMethod extends AbstractWebDavMethod {

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
        if (!entity.isLocked()) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        store.unlock(target);
        return AbstractBasicResponse.createNoContent(entity);
    }
}
