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
package de.shadowhunt.servlet.methods;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Store;

public class UnlockMethod extends AbstractWebDavMethod {

    public static final String METHOD = "UNLOCK";

    private static final long serialVersionUID = 1L;

    public UnlockMethod(final Store store) {
        super(METHOD, store);
    }

    @Override
    public WebDavResponse service(final Path path, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(path)) {
            return AbstractBasicResponse.createNotFound();
        }

        final Entity entity = store.getEntity(path);
        if (!entity.isLocked()) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        if (hasLockProblem(entity, request, "Lock-Token")) {
            return AbstractBasicResponse.createLocked(entity);
        }
        store.unlock(path);
        return AbstractBasicResponse.createNoContent(entity);
    }
}
